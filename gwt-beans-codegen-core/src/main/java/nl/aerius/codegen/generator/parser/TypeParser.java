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
     * @deprecated Use generateParsingCodeInto instead for recursive generation.
     */
    @Deprecated
    CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage);

    /**
     * @deprecated Use generateParsingCodeInto instead for recursive generation.
     */
    @Deprecated
    CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage, String fieldName);

    /**
     * Generates code to parse the given type and appends it to the builder.
     *
     * @param code             The CodeBlock.Builder to add generated code to.
     * @param type             The Type to parse.
     * @param objVarName       The variable name of the parent JSONObjectHandle.
     * @param parserPackage    The package for generated parsers.
     * @param accessExpression A CodeBlock representing how to access the raw JSON data
     *                         for this type from the objVarName (e.g., obj.getObject("fieldName"),
     *                         parentObj.getObject(keyVar), parentArray.getObject(indexVar)).
     *                         For top-level fields, this might be obj.get("fieldName").
     *                         For map values, this should be accessing the parent map's object via the key.
     *                         For list elements, this should be accessing the parent array via index.
     * @param level            The current nesting level (for variable scoping, starting at 1).
     * @return The name of the variable declared within the generated code block
     *         that holds the final parsed value.
     */
    String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression, int level);
} 