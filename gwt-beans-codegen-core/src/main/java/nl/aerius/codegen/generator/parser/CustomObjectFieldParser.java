package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for custom object fields that require their own parsers.
 */
public class CustomObjectFieldParser implements FieldParser {
  private final Map<String, String> customParserImports;

  public CustomObjectFieldParser(Map<String, String> customParserImports) {
    this.customParserImports = customParserImports;
  }

  @Override
  public boolean canHandle(Field field) {
    Class<?> fieldType = field.getType();
    // Handle any non-primitive type that isn't already handled by other parsers
    return !fieldType.isPrimitive() &&
        !Collection.class.isAssignableFrom(fieldType) &&
        !Map.class.isAssignableFrom(fieldType) &&
        !fieldType.isArray() &&
        !fieldType.isEnum();
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    final CodeBlock.Builder code = CodeBlock.builder();
    final String fieldName = field.getName();
    final Class<?> fieldType = field.getType();
    final String typeName = fieldType.getSimpleName();

    code.beginControlFlow(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, false))
        .addStatement("config.set$L($T.parse($L.getObject($S)))",
            ParserCommonUtils.capitalize(fieldName),
            ParserWriterUtils.determineParserClassName(typeName, parserPackage),
            objVarName,
            fieldName)
        .endControlFlow();

    return code.build();
  }
}