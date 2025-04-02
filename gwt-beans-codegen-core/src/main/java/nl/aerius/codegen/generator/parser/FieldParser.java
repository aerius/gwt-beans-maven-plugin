package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;

import com.palantir.javapoet.CodeBlock;

/**
 * Interface for field parsers that generate code to parse specific field types.
 */
public interface FieldParser {

  /**
   * Checks if this parser can handle the given field type.
   * 
   * @param field The field to check
   * @return true if this parser can handle the field
   */
  boolean canHandle(Field field);

  /**
   * Generates code to parse the field from a JSON object.
   * 
   * @param field         The field to parse
   * @param objVarName    The variable name of the JSON object
   * @param parserPackage The package where generated parsers are located
   * @return A code block that parses the field
   */
  CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage);

  /**
   * Generates code to parse the field from a JSON object.
   * 
   * @param field         The field to parse
   * @param objVarName    The variable name of the JSON object
   * @param parserPackage The package where generated parsers are located
   * @param fieldName     The name of the field being parsed, or null if parsing a type directly
   * @return A code block that parses the field
   */
  CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage, String fieldName);
}