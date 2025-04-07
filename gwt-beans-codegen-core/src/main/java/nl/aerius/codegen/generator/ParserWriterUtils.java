package nl.aerius.codegen.generator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

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

/**
 * Utility class containing shared code for parser generation.
 */
public final class ParserWriterUtils {
  // Track custom parser imports
  private static final Map<String, String> customParserImports = new HashMap<>();

  private static final Map<Type, String> ELEMENT_TYPE_TO_ARRAY_GETTER = new HashMap<>();

  // Field parsers
  private static final TypeParser SIMPLE_FIELD_PARSER = new SimpleFieldParser();
  private static final TypeParser ENUM_FIELD_PARSER = new EnumFieldParser();
  private static final TypeParser COLLECTION_FIELD_PARSER = new CollectionFieldParser();
  private static final TypeParser MAP_FIELD_PARSER = new MapFieldParser();
  private static final TypeParser PRIMITIVE_ARRAY_FIELD_PARSER = new PrimitiveArrayFieldParser();
  private static final TypeParser CUSTOM_OBJECT_FIELD_PARSER = new CustomObjectFieldParser(
      customParserImports);

  // Array for easy iteration in dispatcher
  private static final TypeParser[] PARSERS = {
      SIMPLE_FIELD_PARSER,
      ENUM_FIELD_PARSER,
      MAP_FIELD_PARSER,
      COLLECTION_FIELD_PARSER,
      PRIMITIVE_ARRAY_FIELD_PARSER,
      CUSTOM_OBJECT_FIELD_PARSER
  };

  static {
    // Initialize element type to array getter method mapping
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(String.class, "getStringArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Integer.class, "getIntegerArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Double.class, "getNumberArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Boolean.class, "getBooleanArray");
  }

  private ParserWriterUtils() {
    // Utility class, no instantiation
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
  public static void registerCustomParser(String typeName, String packageName) {
    customParserImports.put(typeName + "Parser", packageName + "." + typeName + "Parser");
  }

  /**
   * Main entry point for generating a parser class.
   * Creates both parse(String) and parse(JSONObjectHandle) methods.
   */
  public static void generateParserForFields(TypeSpec.Builder typeSpec, Class<?> targetClass, String parserPackage) {
    typeSpec.addMethod(createStringParseMethod(targetClass));
    typeSpec.addMethod(createObjectParseMethod(targetClass, parserPackage));
    typeSpec.addMethod(createConfigParseMethod(targetClass, parserPackage));
  }

  /**
   * Creates a new parser type specification with standard annotations.
   */
  public static TypeSpec.Builder createParserTypeSpec(String className, String generatorName, String generatorDetails) {
    return TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(ParserCommonUtils.createGeneratedAnnotation(generatorName, generatorDetails));
  }

  /**
   * Writes the generated parser to a file.
   */
  public static void writeParserToFile(String outputDir, String parserPackage, TypeSpec typeSpec, String className)
      throws IOException {
    final JavaFile javaFile = JavaFile.builder(parserPackage, typeSpec)
        .skipJavaLangImports(true)
        .indent("  ")
        .build();

    final Path outputPath = Paths.get(outputDir, className + ".java");

    Files.createDirectories(Paths.get(outputDir));
    Files.writeString(outputPath, javaFile.toString());
  }

  /**
   * Determines which parser to use for a given type, either custom or generated.
   * 
   * @param typeName      The simple name of the type to get a parser for
   * @param parserPackage The package where generated parsers are located
   * @return A JavaPoet ClassName representing the appropriate parser to use
   */
  public static ClassName determineParserClassName(String typeName, String parserPackage) {
    if (hasCustomParser(typeName)) {
      String customParserFQN = getCustomParserFQN(typeName);
      String packageName = customParserFQN.substring(0, customParserFQN.lastIndexOf('.'));
      String simpleName = customParserFQN.substring(customParserFQN.lastIndexOf('.') + 1);
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
  public static ClassName determineParserClassName(Class<?> targetClass, String parserPackage) {
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
  public static ClassName determineParserClassName(ClassName className, String parserPackage) {
    return determineParserClassName(className.simpleName(), parserPackage);
  }

  /**
   * Determines which parser to use for a given Type, either custom or generated.
   * 
   * @param type          The Type to get a parser for
   * @param parserPackage The package where generated parsers are located
   * @return A JavaPoet ClassName representing the appropriate parser to use
   */
  public static ClassName determineParserClassName(Type type, String parserPackage) {
    if (type instanceof Class<?>) {
      return determineParserClassName((Class<?>) type, parserPackage);
    } else if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) type;
      Type rawType = paramType.getRawType();
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
   * Cleans up the output directory by removing all .java files.
   */
  public static void clearOutputDirectory(String outputDir) throws IOException {
    Path outputPath = Paths.get(outputDir);
    if (Files.exists(outputPath)) {
      try (Stream<Path> files = Files.list(outputPath)) {
        files.filter(path -> path.toString().endsWith(".java"))
            .forEach(path -> {
              try {
                Files.delete(path);
              } catch (IOException e) {
                throw new RuntimeException("Failed to delete file: " + path, e);
              }
            });
      }
    }
  }

  /**
   * Checks if a custom parser exists for the given type.
   * 
   * @param typeName The simple name of the type
   * @return true if a custom parser is registered for this type
   */
  public static boolean hasCustomParser(String typeName) {
    return customParserImports.containsKey(typeName + "Parser");
  }

  /**
   * Gets the fully qualified name of a custom parser for the given type.
   * 
   * @param typeName The simple name of the type
   * @return The fully qualified name of the custom parser, or null if none exists
   */
  public static String getCustomParserFQN(String typeName) {
    return customParserImports.get(typeName + "Parser");
  }

  private static MethodSpec createStringParseMethod(Class<?> targetClass) {
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

  private static MethodSpec createObjectParseMethod(Class<?> targetClass, String parserPackage) {
    final ClassName targetClassName = ClassName.get(targetClass);
    final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("parse")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(targetClassName)
        .addParameter(ParserCommonUtils.getJSONObjectHandle(), ParserCommonUtils.BASE_OBJECT_PARAM_NAME, Modifier.FINAL);

    methodBuilder.beginControlFlow("if ($L == null)", ParserCommonUtils.BASE_OBJECT_PARAM_NAME)
        .addStatement("return null")
        .endControlFlow();

    // Check if the class is abstract
    if (targetClass.isInterface()) {
      methodBuilder.addStatement("return null");
    } else if (java.lang.reflect.Modifier.isAbstract(targetClass.getModifiers())) {
      methodBuilder.addStatement("throw new UnsupportedOperationException(\"Cannot create an instance of an abstract class\")");
    } else {
      methodBuilder.addStatement("final $T config = new $T()", targetClass, targetClass)
          .addStatement("parse($L, config)", ParserCommonUtils.BASE_OBJECT_PARAM_NAME)
          .addStatement("return config");
    }

    return methodBuilder.build();
  }

  private static MethodSpec createConfigParseMethod(Class<?> targetClass, String parserPackage) {
    final ClassName targetClassName = ClassName.get(targetClass);
    final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("parse")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(void.class)
        .addParameter(ParserCommonUtils.getJSONObjectHandle(), ParserCommonUtils.BASE_OBJECT_PARAM_NAME, Modifier.FINAL)
        .addParameter(targetClassName, "config", Modifier.FINAL);

    methodBuilder.beginControlFlow("if ($L == null)", ParserCommonUtils.BASE_OBJECT_PARAM_NAME)
        .addStatement("return")
        .endControlFlow();

    // Parse fields from parent class if any
    Class<?> superclass = targetClass.getSuperclass();
    if (superclass != null && superclass != Object.class) {
      methodBuilder.addComment(String.format("Parse fields from parent class (%s)", superclass.getSimpleName()))
          .addStatement("$T.parse($L, config)", determineParserClassName(superclass, parserPackage), ParserCommonUtils.BASE_OBJECT_PARAM_NAME);
    }

    // Process all fields
    for (Field field : targetClass.getDeclaredFields()) {
      if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
          && !java.lang.reflect.Modifier.isTransient(field.getModifiers())
          && !field.isSynthetic()) {

        // ==> INSERT @JsonIgnore CHECK HERE <==
        try {
          Class<? extends java.lang.annotation.Annotation> jsonIgnoreAnnotation = (Class<? extends java.lang.annotation.Annotation>) Class
              .forName("com.fasterxml.jackson.annotation.JsonIgnore");
          if (field.isAnnotationPresent(jsonIgnoreAnnotation)) {
            methodBuilder.addCode("\n"); // Add newline for spacing
            methodBuilder.addComment("Skipping ignored field: $L", field.getName());
            continue; // Skip processing this ignored field
          }
        } catch (ClassNotFoundException e) {
          // Log or handle the case where JsonIgnore annotation class is not available on the classpath during generation
          methodBuilder.addCode("\n");
          methodBuilder.addComment("WARNING: Cannot check for @JsonIgnore, com.fasterxml.jackson.annotation.JsonIgnore not found.");
          // Proceed without checking - might generate code for ignored fields if annotation is used but class not found
        }
        // ==> END @JsonIgnore CHECK <==

        methodBuilder.addCode("\n");
        methodBuilder.addComment("Parse $L", field.getName());
        // Determine if null check is required (true for non-primitives)
        boolean requireNonNull = !ParserCommonUtils.isPrimitiveType(field.getGenericType());
        methodBuilder.addCode(ParserCommonUtils.createFieldExistsCheck(
            ParserCommonUtils.BASE_OBJECT_PARAM_NAME, // Use constant here
            field.getName(),
            requireNonNull, // Pass determined value
            innerCode -> {
              // Pass CodeBlock representing the field name string literal
              CodeBlock fieldAccess = ParserCommonUtils.createFieldAccessCode(
                  field.getGenericType(),
                  ParserCommonUtils.BASE_OBJECT_PARAM_NAME, // Use constant here
                  CodeBlock.of("$S", field.getName()));

              String resultVar = dispatchGenerateParsingCodeInto(
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
  public static String dispatchGenerateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage,
      CodeBlock accessExpression, int level, Type fieldType) {
    for (TypeParser parser : PARSERS) {
      if (parser.canHandle(type)) {
        // Call 7-parameter version
        return parser.generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, fieldType);
      }
    }

    // If no parser handled the type, add a placeholder or throw an error
    String placeholderVar = "level" + level + "UnsupportedValue";
    code.addStatement("$T $L = null; // Type not supported: $L", Object.class, placeholderVar, type.getTypeName());
    // Alternatively, throw an exception:
    // throw new IllegalArgumentException("No parser found for type: " + type.getTypeName());
    return placeholderVar;
  }
}
