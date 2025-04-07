package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.processing.Generated;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

/**
 * Common utilities for parser generation.
 */
public final class ParserCommonUtils {
  // JSON handling constants - Always use the AERIUS JSONObjectHandle and JSONArrayHandle for GWT target
  private static final ClassName JSON_OBJECT_HANDLE = ClassName.get("nl.aerius.wui.service.json", "JSONObjectHandle");
  private static final ClassName JSON_ARRAY_HANDLE = ClassName.get("nl.aerius.wui.service.json", "JSONArrayHandle");

  // Java standard types
  public static final ClassName STRING = ClassName.get(String.class);

  // Parameter name constants
  public static final String BASE_OBJECT_PARAM_NAME = "baseObj";

  // Added from Enum/Collection parsers
  private static final String JSON_CREATOR_ANNOTATION = "com.fasterxml.jackson.annotation.JsonCreator";

  private ParserCommonUtils() {
    // Utility class, no instantiation
  }

  /**
   * Gets the JSON object handle class.
   */
  public static ClassName getJSONObjectHandle() {
    // Always return the AERIUS JSONObjectHandle
    return JSON_OBJECT_HANDLE;
  }

  /**
   * Gets the JSON array handle class.
   */
  public static ClassName getJSONArrayHandle() {
    return JSON_ARRAY_HANDLE;
  }

  public static ClassName getHashMap() {
    return ClassName.get("java.util", "HashMap");
  }

  public static ClassName getArrayList() {
    return ClassName.get("java.util", "ArrayList");
  }

  public static ClassName getHashSet() {
    return ClassName.get("java.util", "HashSet");
  }

  /**
   * Creates a Generated annotation for parser classes.
   *
   * @param generatorName    The name of the generator tool (e.g., class name).
   * @param generatorDetails Additional details like version and Git hash.
   * @return The configured AnnotationSpec.
   */
  public static AnnotationSpec createGeneratedAnnotation(String generatorName, String generatorDetails) {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", generatorName) // Use generator name for value
        // .addMember("date", "$S", timestamp)        // Remove date element
        .addMember("comments", "$S", generatorDetails) // Use details for comments
        .build();
  }

  /**
   * Helper method to check if a field exists and is not null in a JSON object.
   * For primitive types, only checks existence since they cannot be null.
   * For wrapper types and objects, checks both existence and non-null.
   * 
   * @param objVarName     The variable name of the JSON object
   * @param fieldName      The name of the field to check
   * @param requireNonNull Whether to also check that the field is not null (ignored for primitive types)
   * @param body          The code to execute inside the if block
   * @return A code block with the if statement
   */
  public static CodeBlock createFieldExistsCheck(String objVarName, String fieldName, boolean requireNonNull, Consumer<CodeBlock.Builder> body) {
    final CodeBlock.Builder code = CodeBlock.builder();
    // Use CodeBlock for fieldName to handle potential variables vs String literals if needed later
    CodeBlock fieldNameBlock = CodeBlock.of("$S", fieldName);
    if (requireNonNull) {
      code.beginControlFlow("if ($L.has($L) && !$L.isNull($L))", objVarName, fieldNameBlock, objVarName, fieldNameBlock);
    } else {
      code.beginControlFlow("if ($L.has($L))", objVarName, fieldNameBlock);
    }
    body.accept(code);
    code.endControlFlow();
    return code.build();
  }

  /**
   * Capitalizes the first letter of a string.
   */
  public static String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  /**
   * Generates a variable name based on the nesting level.
   * Level 1 uses the base suffix directly (e.g., "value", "map").
   * Levels > 1 prepend "levelN" (e.g., "level2Value", "level3Map").
   *
   * @param level       The nesting level (must be >= 1).
   * @param baseSuffix  The base name suffix (e.g., "Value", "Map", "List", "Obj", "Key").
   * @return The generated variable name.
   * @throws IllegalArgumentException if level < 1.
   */
  public static String getVariableNameForLevel(int level, String baseSuffix) {
    if (level < 1) {
      throw new IllegalArgumentException("Level cannot be less than 1");
    }

    // Default suffix to "value" if null or empty, handle upfront
    if (baseSuffix == null || baseSuffix.isEmpty()) {
      return getVariableNameForLevel(level, "value");
    }

    if (level == 1) {
      // Level 1: Ensure the first letter is lowercase (no need for null check now)
      String firstChar = baseSuffix.substring(0, 1).toLowerCase();
      return firstChar + baseSuffix.substring(1);
    } else {
      // Level > 1: Prepend "levelN" and Capitalize the suffix (no need for null check now)
      String capitalizedSuffix = Character.toUpperCase(baseSuffix.charAt(0)) + baseSuffix.substring(1);
      return "level" + level + capitalizedSuffix;
    }
  }

  /**
   * Creates a CodeBlock representing the access to a field's data within a JSONObjectHandle.
   * Determines the correct getter method (getObject, getString, getInteger, getArray, etc.)
   * based on the provided type.
   *
   * @param type                     The Type of the data being accessed.
   * @param objVarName               The variable name of the JSONObjectHandle.
   * @param keyOrFieldNameExpression A CodeBlock representing the key or field name (e.g., "fieldName" or a variable like levelXKey).
   * @return A CodeBlock like `objVar.getObject(keyOrFieldNameExpression)` or `objVar.getString(keyOrFieldNameExpression)`.
   */
  public static CodeBlock createFieldAccessCode(Type type, String objVarName, CodeBlock keyOrFieldNameExpression) {
    if (type instanceof Class<?>) {
      Class<?> clazz = (Class<?>) type;
      if (clazz.equals(String.class)) {
        return CodeBlock.of("$L.getString($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
        return CodeBlock.of("$L.getInteger($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.equals(Short.class) || clazz.equals(short.class)) {
        return CodeBlock.of("$L.getInteger($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.equals(Byte.class) || clazz.equals(byte.class)) {
        return CodeBlock.of("$L.getInteger($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.equals(Long.class) || clazz.equals(long.class)) {
        return CodeBlock.of("$L.getLong($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.equals(Double.class) || clazz.equals(double.class)) {
        return CodeBlock.of("$L.getNumber($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.equals(Float.class) || clazz.equals(float.class)) {
        return CodeBlock.of("$L.getNumber($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
        return CodeBlock.of("$L.getBoolean($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.equals(Character.class) || clazz.equals(char.class)) {
        return CodeBlock.of("$L.getString($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.isEnum()) {
        // Enums are typically stored as strings, the parser will handle valueOf
        return CodeBlock.of("$L.getString($L)", objVarName, keyOrFieldNameExpression);
      } else if (clazz.isArray()) {
        // JSON arrays correspond to getArray
        return CodeBlock.of("$L.getArray($L)", objVarName, keyOrFieldNameExpression);
      } else {
        // Default to getObject for complex objects or unknown types that aren't collections/maps
        return CodeBlock.of("$L.getObject($L)", objVarName, keyOrFieldNameExpression);
      }
    } else if (type instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) type;
      Type rawType = paramType.getRawType();
      if (rawType instanceof Class<?>) {
        Class<?> rawClass = (Class<?>) rawType;
        if (Map.class.isAssignableFrom(rawClass)) {
          // Maps are represented as JSON objects
          return CodeBlock.of("$L.getObject($L)", objVarName, keyOrFieldNameExpression);
        } else if (List.class.isAssignableFrom(rawClass) || Collection.class.isAssignableFrom(rawClass)) {
          // Lists/Collections are represented as JSON arrays
          return CodeBlock.of("$L.getArray($L)", objVarName, keyOrFieldNameExpression);
        }
      }
      // Default for other parameterized types (assume object)
      return CodeBlock.of("$L.getObject($L)", objVarName, keyOrFieldNameExpression);
    }

    // Fallback for other types (like TypeVariable, WildcardType) - default to getObject
    // Log warning or throw error might be better long-term
    System.err.println("Warning: Defaulting to getObject() for unknown type in createFieldAccessCode: " + type.getTypeName());
    return CodeBlock.of("$L.getObject($L)", objVarName, keyOrFieldNameExpression);
  }

  /**
   * Checks if the given type represents a Java primitive type.
   *
   * @param type The type to check.
   * @return true if the type is a primitive (int, long, boolean, etc.), false otherwise.
   */
  public static boolean isPrimitiveType(Type type) {
    return (type instanceof Class<?>) && ((Class<?>) type).isPrimitive();
  }

  /**
   * Checks if the given type is an interface.
   */
  public static boolean isInterface(Type type) {
    return (type instanceof Class<?>) && ((Class<?>) type).isInterface();
  }

  /**
   * Checks if the given type is a wildcard type (?).
   */
  public static boolean isWildcard(Type type) {
    return type instanceof java.lang.reflect.WildcardType;
  }

  /**
   * Checks if the given type or its type arguments contain a wildcard.
   * Recursively checks parameterized types.
   */
  public static boolean containsWildcard(Type type) {
    if (isWildcard(type)) {
      return true;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      for (Type arg : pt.getActualTypeArguments()) {
        if (containsWildcard(arg)) {
          return true;
        }
      }
    }
    // Add check for GenericArrayType if necessary
    // if (type instanceof GenericArrayType) { ... }
    return false;
  }

  /**
   * Finds a static method annotated with @JsonCreator that takes a single String
   * argument and returns the enum type.
   *
   * @param enumType The enum class to inspect.
   * @return The Method object if found, otherwise null.
   */
  public static Method findJsonCreatorMethod(Class<?> enumType) {
    // Check if it's actually an enum first
    if (enumType == null || !enumType.isEnum()) {
      return null;
    }
    try {
      @SuppressWarnings("unchecked")
      Class<? extends java.lang.annotation.Annotation> jsonCreatorClass = (Class<? extends java.lang.annotation.Annotation>) Class
          .forName(JSON_CREATOR_ANNOTATION);

      for (Method method : enumType.getDeclaredMethods()) {
        if (method.isAnnotationPresent(jsonCreatorClass) &&
            Modifier.isStatic(method.getModifiers()) &&
            method.getParameterCount() == 1 &&
            method.getParameterTypes()[0] == String.class &&
            method.getReturnType() == enumType) {
          return method;
        }
      }
    } catch (ClassNotFoundException e) {
      // Annotation not found, expected if Jackson is not used/available
    } catch (Exception e) {
      System.err.println("Warning: Error checking for @JsonCreator on " + enumType.getName() + ": " + e.getMessage());
    }
    return null;
  }
}