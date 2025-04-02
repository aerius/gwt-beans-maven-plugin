package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for custom object fields that require their own parsers.
 */
public class CustomObjectFieldParser implements TypeParser {
  private final Map<String, String> customParserImports;

  public CustomObjectFieldParser(Map<String, String> customParserImports) {
    this.customParserImports = customParserImports;
  }

  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof Class<?>)) {
      return false;
    }
    Class<?> clazz = (Class<?>) type;
    // Handle any non-primitive type that isn't already handled by other parsers
    return !clazz.isPrimitive() &&
        !Collection.class.isAssignableFrom(clazz) &&
        !Map.class.isAssignableFrom(clazz) &&
        !clazz.isArray() &&
        !clazz.isEnum();
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    return generateParsingCode(field.getType(), objVarName, parserPackage, field.getName());
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage, String fieldName) {
    return generateParsingCode(field.getType(), objVarName, parserPackage, fieldName);
  }

  @Override
  public CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage) {
    return generateParsingCode(type, objVarName, parserPackage, "value");
  }

  @Override
  public CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage, String fieldName) {
    if (!(type instanceof Class<?>)) {
      throw new IllegalArgumentException("CustomObjectFieldParser only handles Class types");
    }
    Class<?> clazz = (Class<?>) type;
    final String typeName = clazz.getSimpleName();

    return ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true, code -> {
          code.addStatement("config.set$L($T.parse($L.getObject($S)))",
          ParserCommonUtils.capitalize(fieldName),
          ParserWriterUtils.determineParserClassName(typeName, parserPackage),
          objVarName,
          fieldName);
    });
  }
}