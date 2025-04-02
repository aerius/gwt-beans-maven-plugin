package nl.aerius.codegen.generator.parser;

import java.util.function.Consumer;

import javax.annotation.processing.Generated;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

/**
 * Common utilities for parser generation.
 */
public final class ParserCommonUtils {
  // JSON handling constants - Always use the AERIUS JSONObjectHandle
  private static final ClassName JSON_OBJECT_HANDLE = ClassName.get("nl.aerius.wui.service.json", "JSONObjectHandle");

  // Java standard types
  public static final ClassName STRING = ClassName.get(String.class);

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
   */
  public static AnnotationSpec createGeneratedAnnotation(String timestamp) {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", "nl.aerius.codegen.ParserGenerator")
        .addMember("date", "$S", timestamp)
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
    if (requireNonNull) {
      code.beginControlFlow("if ($L.has($S) && !$L.isNull($S))", objVarName, fieldName, objVarName, fieldName);
    } else {
      code.beginControlFlow("if ($L.has($S))", objVarName, fieldName);
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
}