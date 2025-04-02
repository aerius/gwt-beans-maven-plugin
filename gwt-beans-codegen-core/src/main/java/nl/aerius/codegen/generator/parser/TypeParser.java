package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Type;

import com.palantir.javapoet.CodeBlock;

/**
 * Interface for parsers that can handle both field-based and type-based parsing.
 * This extends FieldParser to maintain backward compatibility while adding support
 * for type-based parsing.
 */
public interface TypeParser extends FieldParser {
    /**
     * Checks if this parser can handle the given type.
     * 
     * @param type The type to check
     * @return true if this parser can handle the type
     */
    boolean canHandle(Type type);

    /**
     * Generates code to parse a value of the given type from a JSON object.
     * This is the new type-based parsing method that supports nested types.
     * 
     * @param type          The type to generate parsing code for
     * @param objVarName    The name of the JSON object variable
     * @param parserPackage The package where generated parsers are located
     * @return The generated code block
     */
    CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage);

    /**
     * Generates code to parse a value of the given type from a JSON object.
     * This is the new type-based parsing method that supports nested types.
     * 
     * @param type          The type to generate parsing code for
     * @param objVarName    The name of the JSON object variable
     * @param parserPackage The package where generated parsers are located
     * @param fieldName     The name of the field being parsed, or null if parsing a type directly
     * @return The generated code block
     */
    CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage, String fieldName);
} 