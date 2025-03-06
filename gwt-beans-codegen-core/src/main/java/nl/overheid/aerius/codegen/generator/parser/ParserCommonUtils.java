package nl.overheid.aerius.codegen.generator.parser;

import javax.annotation.processing.Generated;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;

/**
 * Common utilities for parser generation.
 */
public final class ParserCommonUtils {
  // JSON handling constants
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
    return JSON_OBJECT_HANDLE;
  }

  /**
   * Creates a Generated annotation for parser classes.
   */
  public static AnnotationSpec createGeneratedAnnotation(String timestamp) {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", "nl.overheid.aerius.codegen.ParserGenerator")
        .addMember("date", "$S", timestamp)
        .build();
  }

  /**
   * Helper method to check if a field exists and is not null in a JSON object.
   * 
   * @param objVarName     The variable name of the JSON object
   * @param fieldName      The name of the field to check
   * @param requireNonNull Whether to also check that the field is not null
   * @return A code block with the if statement condition
   */
  public static String createFieldExistsCheck(String objVarName, String fieldName, boolean requireNonNull) {
    return requireNonNull
        ? String.format("if (%s.has(\"%s\") && !%s.isNull(\"%s\"))", objVarName, fieldName, objVarName, fieldName)
        : String.format("if (%s.has(\"%s\"))", objVarName, fieldName);
  }

  /**
   * Capitalizes the first letter of a string.
   */
  public static String capitalize(String str) {
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }
}