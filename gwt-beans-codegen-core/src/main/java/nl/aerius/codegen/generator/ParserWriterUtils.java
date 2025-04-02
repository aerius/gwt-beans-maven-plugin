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

/**
 * Utility class containing shared code for parser generation.
 */
public final class ParserWriterUtils {
  // Track custom parser imports
  private static final Map<String, String> customParserImports = new HashMap<>();

  private static final Map<Type, String> ELEMENT_TYPE_TO_ARRAY_GETTER = new HashMap<>();

  // Field parsers
  private static final SimpleFieldParser SIMPLE_FIELD_PARSER = new SimpleFieldParser();
  private static final EnumFieldParser ENUM_FIELD_PARSER = new EnumFieldParser();
  private static final CollectionFieldParser COLLECTION_FIELD_PARSER = new CollectionFieldParser();
  private static final MapFieldParser MAP_FIELD_PARSER = new MapFieldParser();
  private static final PrimitiveArrayFieldParser PRIMITIVE_ARRAY_FIELD_PARSER = new PrimitiveArrayFieldParser();
  private static final CustomObjectFieldParser CUSTOM_OBJECT_FIELD_PARSER = new CustomObjectFieldParser(
      customParserImports);

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
  public static TypeSpec.Builder createParserTypeSpec(String className, String generatedTimestamp) {
    return TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(ParserCommonUtils.createGeneratedAnnotation(generatedTimestamp));
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
    System.out.println("Writing parser to: " + outputPath.toAbsolutePath());

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
        .addParameter(ParserCommonUtils.getJSONObjectHandle(), "obj", Modifier.FINAL);

    methodBuilder.beginControlFlow("if (obj == null)")
        .addStatement("return null")
        .endControlFlow();

    // Check if the class is abstract
    if (java.lang.reflect.Modifier.isAbstract(targetClass.getModifiers())) {
      methodBuilder.addStatement("throw new UnsupportedOperationException(\"Cannot create an instance of an abstract class\")");
    } else {
      methodBuilder.addStatement("final $T config = new $T()", targetClass, targetClass)
          .addStatement("parse(obj, config)")
          .addStatement("return config");
    }

    return methodBuilder.build();
  }

  private static MethodSpec createConfigParseMethod(Class<?> targetClass, String parserPackage) {
    final ClassName targetClassName = ClassName.get(targetClass);
    final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("parse")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(void.class)
        .addParameter(ParserCommonUtils.getJSONObjectHandle(), "obj", Modifier.FINAL)
        .addParameter(targetClassName, "config", Modifier.FINAL);

    methodBuilder.beginControlFlow("if (obj == null)")
        .addStatement("return")
        .endControlFlow();

    // Parse fields from parent class if any
    Class<?> superclass = targetClass.getSuperclass();
    if (superclass != null && superclass != Object.class) {
      methodBuilder.addComment(String.format("Parse fields from parent class (%s)", superclass.getSimpleName()))
          .addStatement("$T.parse(obj, config)", determineParserClassName(superclass, parserPackage));
    }

    // Process all fields
    for (Field field : targetClass.getDeclaredFields()) {
      if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
          && !java.lang.reflect.Modifier.isTransient(field.getModifiers())
          && !field.isSynthetic()) {
        methodBuilder.addCode("\n");
        methodBuilder.addComment("Parse $L", field.getName());
        methodBuilder.addCode(generateFieldParsingCode(field, "obj", parserPackage));
      }
    }

    return methodBuilder.build();
  }

  private static CodeBlock generateFieldParsingCode(Field field, String objVarName, String parserPackage) {
    // Try the SimpleFieldParser first
    if (SIMPLE_FIELD_PARSER.canHandle(field)) {
      return SIMPLE_FIELD_PARSER.generateParsingCode(field, objVarName, parserPackage);
    }

    // Try the EnumFieldParser
    if (ENUM_FIELD_PARSER.canHandle(field)) {
      return ENUM_FIELD_PARSER.generateParsingCode(field, objVarName, parserPackage);
    }

    // Try the CollectionFieldParser (includes object arrays)
    if (COLLECTION_FIELD_PARSER.canHandle(field)) {
      return COLLECTION_FIELD_PARSER.generateParsingCode(field, objVarName, parserPackage);
    }

    // Try the MapFieldParser
    if (MAP_FIELD_PARSER.canHandle(field)) {
      return MAP_FIELD_PARSER.generateParsingCode(field, objVarName, parserPackage);
    }

    // Try the PrimitiveArrayFieldParser
    if (PRIMITIVE_ARRAY_FIELD_PARSER.canHandle(field)) {
      return PRIMITIVE_ARRAY_FIELD_PARSER.generateParsingCode(field, objVarName, parserPackage);
    }

    // Try the CustomObjectFieldParser
    if (CUSTOM_OBJECT_FIELD_PARSER.canHandle(field)) {
      return CUSTOM_OBJECT_FIELD_PARSER.generateParsingCode(field, objVarName, parserPackage);
    }

    throw new IllegalArgumentException("No parser found for field: " + field.getName());
  }
}