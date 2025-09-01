package nl.aerius.codegen.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;

import nl.aerius.codegen.generator.parser.CollectionFieldParser;
import nl.aerius.codegen.generator.parser.CustomObjectFieldParser;
import nl.aerius.codegen.generator.parser.EnumFieldParser;
import nl.aerius.codegen.generator.parser.MapFieldParser;
import nl.aerius.codegen.generator.parser.ParserCommonUtils;
import nl.aerius.codegen.generator.parser.PrimitiveArrayFieldParser;
import nl.aerius.codegen.generator.parser.SimpleFieldParser;
import nl.aerius.codegen.generator.parser.TypeParser;
import nl.aerius.codegen.util.ClassFinder;
import nl.aerius.codegen.util.Logger;

/**
 * Utility class containing shared code for parser generation.
 */
public final class ParserWriterUtils {
  // Track custom parser imports
  private static final Map<String, String> customParserImports = new HashMap<>();

  private static final Map<Type, String> ELEMENT_TYPE_TO_ARRAY_GETTER = new HashMap<>();

  // Field parsers
  private static final TypeParser SIMPLE_FIELD_PARSER = new SimpleFieldParser();
  private static final TypeParser PRIMITIVE_ARRAY_FIELD_PARSER = new PrimitiveArrayFieldParser();
  private static final TypeParser CUSTOM_OBJECT_FIELD_PARSER = new CustomObjectFieldParser(
      customParserImports);

  // Array for easy iteration in dispatcher
  private static TypeParser[] PARSERS = new TypeParser[0];

  static {
    // Initialize element type to array getter method mapping
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(String.class, "getStringArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Integer.class, "getIntegerArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Double.class, "getNumberArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Boolean.class, "getBooleanArray");
  }

  private static TypeParser enumFieldParser;
  private static TypeParser collectionFieldParser;
  private static TypeParser mapFieldParser;

  private ParserWriterUtils() {
    // Utility class, no instantiation
  }

  public static void initParsers(final ClassFinder classFinder, final Logger logger) {
    enumFieldParser = new EnumFieldParser(classFinder, logger);
    collectionFieldParser = new CollectionFieldParser(classFinder, logger);
    mapFieldParser = new MapFieldParser(logger);

    PARSERS = new TypeParser[] {
        SIMPLE_FIELD_PARSER,
        enumFieldParser,
        mapFieldParser,
        collectionFieldParser,
        PRIMITIVE_ARRAY_FIELD_PARSER,
        CUSTOM_OBJECT_FIELD_PARSER
    };
  }

  /**
   * Clears the registry of custom parsers.
   * This should be called before scanning for custom parsers to ensure a clean
   * state.
   */
  public static void clearCustomParserRegistry() {
    customParserImports.clear();
  }

  /**
   * Registers a custom parser for import tracking.
   *
   * @param typeName    The simple name of the type that has a custom parser
   * @param packageName The package name where the custom parser is located
   */
  public static void registerCustomParser(final String typeName, final String packageName) {
    customParserImports.put(typeName + "Parser", packageName + "." + typeName + "Parser");
  }

  /**
   * Main entry point for generating a parser class.
   * Creates both parse(String) and parse(JSONObjectHandle) methods.
   * @param classFinder
   */
  public static void generateParserForFields(final TypeSpec.Builder typeSpec, final Class<?> targetClass, final String parserPackage,
      final ClassFinder classFinder) {
    typeSpec.addMethod(createStringParseMethod(targetClass));
    // Decide which kind of object parse method to generate based on polymorphism
    if (hasJsonTypeInfoWithNameDiscriminator(targetClass)) {
      typeSpec.addMethod(createPolymorphicObjectParseMethod(targetClass, parserPackage));
    } else {
      typeSpec.addMethod(createStandardObjectParseMethod(targetClass, parserPackage));
    }
    typeSpec.addMethod(createConfigParseMethod(targetClass, parserPackage, classFinder));
  }

  /**
   * Creates a new parser type specification with standard annotations.
   */
  public static TypeSpec.Builder createParserTypeSpec(final String className, final String generatorName, final String generatorDetails) {
    return TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(ParserCommonUtils.createGeneratedAnnotation(generatorName, generatorDetails));
  }

  /**
   * Writes the generated parser to a file.
   * @param logger
   */
  public static void writeParserToFile(final String outputDir, final String parserPackage, final TypeSpec typeSpec, final String className,
      final Logger logger) throws IOException {
    final JavaFile javaFile = JavaFile.builder(parserPackage, typeSpec)
        .skipJavaLangImports(true)
        .indent("  ")
        .build();

    final Path outputPath = Paths.get(outputDir, parserPackage.split("\\."));

    Files.createDirectories(outputPath);
    Files.writeString(new File(outputPath.toFile(), className + ".java").toPath(), javaFile.toString());
  }

  /**
   * Determines which parser to use for a given type, either custom or generated.
   *
   * @param typeName      The simple name of the type to get a parser for
   * @param parserPackage The package where generated parsers are located
   * @return A JavaPoet ClassName representing the appropriate parser to use
   */
  public static ClassName determineParserClassName(final String typeName, final String parserPackage) {
    if (hasCustomParser(typeName)) {
      final String customParserFQN = getCustomParserFQN(typeName);
      final String packageName = customParserFQN.substring(0, customParserFQN.lastIndexOf('.'));
      final String simpleName = customParserFQN.substring(customParserFQN.lastIndexOf('.') + 1);
      return ClassName.get(packageName, simpleName);
    } else {
      return ClassName.get(parserPackage, typeName + "Parser");
    }
  }

  /**
   * Determines which parser to use for a given class, either custom or generated.
   *
   * @param targetClass   The class to get a parser for
   * @param parserPackage The package where generated parsers are located
   * @return A JavaPoet ClassName representing the appropriate parser to use
   */
  public static ClassName determineParserClassName(final Class<?> targetClass, final String parserPackage) {
    return determineParserClassName(targetClass.getSimpleName(), parserPackage);
  }

  /**
   * Determines which parser to use for a given ClassName, either custom or
   * generated.
   *
   * @param className     The ClassName to get a parser for
   * @param parserPackage The package where generated parsers are located
   * @return A JavaPoet ClassName representing the appropriate parser to use
   */
  public static ClassName determineParserClassName(final ClassName className, final String parserPackage) {
    return determineParserClassName(className.simpleName(), parserPackage);
  }

  /**
   * Determines which parser to use for a given Type, either custom or generated.
   *
   * @param type          The Type to get a parser for
   * @param parserPackage The package where generated parsers are located
   * @return A JavaPoet ClassName representing the appropriate parser to use
   */
  public static ClassName determineParserClassName(final Type type, final String parserPackage) {
    if (type instanceof Class<?>) {
      return determineParserClassName((Class<?>) type, parserPackage);
    } else if (type instanceof java.lang.reflect.ParameterizedType) {
      final java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) type;
      final Type rawType = paramType.getRawType();
      if (rawType instanceof Class<?>) {
        return determineParserClassName((Class<?>) rawType, parserPackage);
      }
    }
    // Fallback for complex types - might need refinement
    String typeName = type.getTypeName();
    // Basic attempt to get a simple name
    if (typeName.contains("<")) {
      typeName = typeName.substring(0, typeName.indexOf('<'));
    }
    if (typeName.contains(".")) {
      typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
    }
    return determineParserClassName(typeName, parserPackage);
    // throw new IllegalArgumentException("Cannot determine parser class name for type: " + type.getTypeName());
  }

  /**
   * Checks if a custom parser exists for the given type.
   *
   * @param typeName The simple name of the type
   * @return true if a custom parser is registered for this type
   */
  public static boolean hasCustomParser(final String typeName) {
    return customParserImports.containsKey(typeName + "Parser");
  }

  /**
   * Gets the fully qualified name of a custom parser for the given type.
   *
   * @param typeName The simple name of the type
   * @return The fully qualified name of the custom parser, or null if none exists
   */
  public static String getCustomParserFQN(final String typeName) {
    return customParserImports.get(typeName + "Parser");
  }

  private static MethodSpec createStringParseMethod(final Class<?> targetClass) {
    final ClassName targetClassName = ClassName.get(targetClass);
    final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("parse")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(targetClassName)
        .addParameter(ParserCommonUtils.STRING, "jsonText", Modifier.FINAL);

    methodBuilder.beginControlFlow("if (jsonText == null)")
        .addStatement("return null")
        .endControlFlow()
        .addStatement("return parse($T.fromText(jsonText))", ParserCommonUtils.getJSONObjectHandle());

    return methodBuilder.build();
  }

  private static MethodSpec createStandardObjectParseMethod(final Class<?> targetClass, final String parserPackage) {
    final ClassName targetClassName = ClassName.get(targetClass);
    final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("parse")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(targetClassName)
        .addParameter(ParserCommonUtils.getJSONObjectHandle(), ParserCommonUtils.BASE_OBJECT_PARAM_NAME, Modifier.FINAL);

    methodBuilder.beginControlFlow("if ($L == null)", ParserCommonUtils.BASE_OBJECT_PARAM_NAME)
        .addStatement("return null")
        .endControlFlow();

    // Check if the class is abstract or an interface BEFORE attempting instantiation
    if (targetClass.isInterface() || java.lang.reflect.Modifier.isAbstract(targetClass.getModifiers())) {
      // For abstract classes/interfaces, the polymorphic parser or a custom parser should handle it.
      // Throwing here prevents generation of invalid direct instantiation code.
      methodBuilder.addStatement("throw new $T($S + $T.class.getName() + $S)",
          UnsupportedOperationException.class,
          "Cannot directly instantiate abstract class or interface ",
          targetClass,
          ". Use @JsonTypeInfo or a custom parser.");
    } else {
      methodBuilder.addStatement("final $T config = new $T()", targetClass, targetClass)
          .addStatement("parse($L, config)", ParserCommonUtils.BASE_OBJECT_PARAM_NAME)
          .addStatement("return config");
    }

    return methodBuilder.build();
  }

  // New method for polymorphic base class main parse method
  private static MethodSpec createPolymorphicObjectParseMethod(final Class<?> targetClass, final String parserPackage) {
    final ClassName targetClassName = ClassName.get(targetClass); // Base class type
    final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("parse")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(targetClassName) // Returns the base type
        .addParameter(ParserCommonUtils.getJSONObjectHandle(), ParserCommonUtils.BASE_OBJECT_PARAM_NAME, Modifier.FINAL);

    methodBuilder.beginControlFlow("if ($L == null)", ParserCommonUtils.BASE_OBJECT_PARAM_NAME)
        .addStatement("return null")
        .endControlFlow();

    final String discriminatorProperty = getDiscriminatorProperty(targetClass);

    // Generate the combined has/isNull check and specific error message from expected parser
    methodBuilder.beginControlFlow("if (!$L.has($S) || $L.isNull($S))",
        ParserCommonUtils.BASE_OBJECT_PARAM_NAME, discriminatorProperty,
        ParserCommonUtils.BASE_OBJECT_PARAM_NAME, discriminatorProperty)
        .addStatement("throw new $T($S)", RuntimeException.class,
            "Expected string for type discriminator field '" + discriminatorProperty + "', got different type") // Match expected message
        .endControlFlow();

    // Get the typeName *after* the combined check
    methodBuilder.addStatement("final $T typeName = $L.getString($S)", String.class, ParserCommonUtils.BASE_OBJECT_PARAM_NAME, discriminatorProperty);

    // Remove the subsequent null/empty check on typeName as it's not in the expected parser

    // Add switch statement
    methodBuilder.beginControlFlow("switch (typeName)");
    final JsonSubTypes subTypes = targetClass.getAnnotation(JsonSubTypes.class);
    for (final JsonSubTypes.Type subType : subTypes.value()) {
      final String caseName = subType.name();
      final Class<?> subTypeValue = subType.value();
      final ClassName subTypeParserName = determineParserClassName(subTypeValue, parserPackage); // Use helper to get parser name
      methodBuilder.addCode("case $S:\n", caseName);
      // Delegate to the static parse method of the subtype parser
      methodBuilder.addStatement("  return $T.parse($L)", subTypeParserName, ParserCommonUtils.BASE_OBJECT_PARAM_NAME);
    }
    // Default case for unknown type name
    methodBuilder.addCode("default:\n");
    methodBuilder.addStatement("  throw new $T($S + typeName + $S)", RuntimeException.class, "Unknown type name '",
        "' for " + targetClass.getSimpleName());
    methodBuilder.endControlFlow(); // End switch

    return methodBuilder.build();
  }

  private static MethodSpec createConfigParseMethod(final Class<?> targetClass, final String parserPackage, final ClassFinder classFinder) {
    final ClassName targetClassName = ClassName.get(targetClass);
    final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("parse")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(void.class)
        .addParameter(ParserCommonUtils.getJSONObjectHandle(), ParserCommonUtils.BASE_OBJECT_PARAM_NAME, Modifier.FINAL)
        .addParameter(targetClassName, "config", Modifier.FINAL);

    methodBuilder.beginControlFlow("if ($L == null || config == null)", ParserCommonUtils.BASE_OBJECT_PARAM_NAME) // Added check for config != null
        .addStatement("return")
        .endControlFlow();

    // Parse fields from parent class if any
    final Class<?> superclass = targetClass.getSuperclass();
    if (superclass != null && superclass != Object.class) {
      methodBuilder.addComment(String.format("Parse fields from parent class (%s)", superclass.getSimpleName()))
          .addStatement("$T.parse($L, config)", determineParserClassName(superclass, parserPackage), ParserCommonUtils.BASE_OBJECT_PARAM_NAME);
    }

    // Process all fields
    for (final Field field : targetClass.getDeclaredFields()) {
      if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
          && !java.lang.reflect.Modifier.isTransient(field.getModifiers())
          && !field.isSynthetic()) {

        // ==> INSERT @JsonIgnore CHECK HERE <==
        try {
          final Class<? extends java.lang.annotation.Annotation> jsonIgnoreAnnotation = (Class<? extends java.lang.annotation.Annotation>) classFinder
              .forName("com.fasterxml.jackson.annotation.JsonIgnore");
          if (field.isAnnotationPresent(jsonIgnoreAnnotation)) {
            methodBuilder.addCode("\n"); // Add newline for spacing
            methodBuilder.addComment("Skipping ignored field: $L", field.getName());
            continue; // Skip processing this ignored field
          }
        } catch (final ClassNotFoundException e) {
          // Log or handle the case where JsonIgnore annotation class is not available on the classpath during generation
          methodBuilder.addCode("\n");
          methodBuilder.addComment("WARNING: Cannot check for @JsonIgnore, com.fasterxml.jackson.annotation.JsonIgnore not found.");
          // Proceed without checking - might generate code for ignored fields if annotation is used but class not found
        }
        // ==> END @JsonIgnore CHECK <==

        methodBuilder.addCode("\n");
        methodBuilder.addComment("Parse $L", field.getName());
        // Determine if null check is required (true for non-primitives)
        final boolean requireNonNull = !ParserCommonUtils.isPrimitiveType(field.getGenericType());
        methodBuilder.addCode(ParserCommonUtils.createFieldExistsCheck(
            ParserCommonUtils.BASE_OBJECT_PARAM_NAME, // Use constant here
            field.getName(),
            requireNonNull, // Pass determined value
            innerCode -> {
              // Pass CodeBlock representing the field name string literal
              final CodeBlock fieldAccess = ParserCommonUtils.createFieldAccessCode(
                  field.getGenericType(),
                  ParserCommonUtils.BASE_OBJECT_PARAM_NAME, // Use constant here
                  CodeBlock.of("$S", field.getName()));

              final String resultVar = dispatchGenerateParsingCodeInto(
                  innerCode,
                  field.getGenericType(),
                  ParserCommonUtils.BASE_OBJECT_PARAM_NAME, // Use constant here
                  parserPackage,
                  fieldAccess, // Pass the code to access the field's data
                  1, // Start top-level fields at level 1 - Remove last arg
                  field.getGenericType() // Pass fieldType
              );
              innerCode.addStatement("config.set$L($L)", ParserCommonUtils.capitalize(field.getName()), resultVar);
            }));
      }
    }

    return methodBuilder.build();
  }

  /**
   * Dispatches parsing logic to the appropriate TypeParser based on the given type.
   *
   * @param code             The CodeBlock.Builder to add generated code to.
   * @param type             The Type to parse.
   * @param objVarName       The variable name of the *parent* JSONObjectHandle containing the data.
   * @param parserPackage    The package for generated parsers.
   * @param accessExpression A CodeBlock representing how to access the raw JSON data
   *                         for this type from the objVarName (e.g., "keyVar", "indexVar", or for top-level fields,
   *                         an expression like obj.getObject("fieldName")).
   * @param level            The current nesting level (for variable scoping).
   * @param fieldType        The exact generic type of the field/setter parameter.
   * @return The name of the variable declared within the generated code block
   *         that holds the final parsed value.
   */
  public static String dispatchGenerateParsingCodeInto(final CodeBlock.Builder code, final Type type, final String objVarName,
      final String parserPackage,
      final CodeBlock accessExpression, final int level, final Type fieldType) {
    for (final TypeParser parser : PARSERS) {
      if (parser.canHandle(type)) {
        // Call 7-parameter version
        return parser.generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, fieldType);
      }
    }

    // If no parser handled the type, add a placeholder or throw an error
    final String placeholderVar = "level" + level + "UnsupportedValue";
    code.addStatement("$T $L = null; // Type not supported: $L", Object.class, placeholderVar, type.getTypeName());
    // Alternatively, throw an exception:
    // throw new IllegalArgumentException("No parser found for type: " + type.getTypeName());
    return placeholderVar;
  }

  // Helper to check for Polymorphic Base Class annotations
  private static boolean hasJsonTypeInfoWithNameDiscriminator(final Class<?> clazz) {
    final JsonTypeInfo typeInfo = clazz.getAnnotation(JsonTypeInfo.class);
    final JsonSubTypes subTypes = clazz.getAnnotation(JsonSubTypes.class);
    // Check for the specific combination we want to handle
    return typeInfo != null && typeInfo.use() == JsonTypeInfo.Id.NAME && subTypes != null;
  }

  // Helper to get the discriminator property name
  private static String getDiscriminatorProperty(final Class<?> clazz) {
    final JsonTypeInfo typeInfo = clazz.getAnnotation(JsonTypeInfo.class);
    if (typeInfo != null) {
      return typeInfo.property();
    }
    return null; // Should not happen if hasJsonTypeInfoWithNameDiscriminator is true
  }

  // Helper to find the superclass that is the polymorphic base, if any
  private static Class<?> findPolymorphicSuperclass(final Class<?> clazz) {
    Class<?> superclass = clazz.getSuperclass();
    while (superclass != null && superclass != Object.class) {
      if (hasJsonTypeInfoWithNameDiscriminator(superclass)) {
        return superclass;
      }
      superclass = superclass.getSuperclass();
    }
    return null;
  }
}
