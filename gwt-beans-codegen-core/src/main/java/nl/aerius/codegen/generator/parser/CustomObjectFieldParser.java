package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for custom object fields that require their own generated or custom parsers.
 */
public class CustomObjectFieldParser implements TypeParser {
  private final Map<String, String> customParserImports;

  public CustomObjectFieldParser(Map<String, String> customParserImports) {
    this.customParserImports = customParserImports;
  }

  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getGenericType());
  }

  @Override
  public boolean canHandle(Type type) {
    // Check if it's a class type first
    if (type instanceof Class<?>) {
      Class<?> clazz = (Class<?>) type;
        // Handle non-primitive, non-Collection, non-Map, non-array, non-enum types
        return !clazz.isPrimitive() &&
            !Collection.class.isAssignableFrom(clazz) &&
            !Map.class.isAssignableFrom(clazz) &&
            !clazz.isArray() &&
            !clazz.isEnum();
      } else if (type instanceof ParameterizedType) {
        // Potentially handle complex parameterized types if they aren't Collections or Maps
        // Example: CustomGeneric<String>, if we had a parser for CustomGeneric
        Type rawType = ((ParameterizedType) type).getRawType();
        if (rawType instanceof Class<?>) {
          Class<?> rawClass = (Class<?>) rawType;
          return !Collection.class.isAssignableFrom(rawClass) &&
              !Map.class.isAssignableFrom(rawClass);
        }
      }
      // Doesn't handle other types like TypeVariable, WildcardType, GenericArrayType directly
      return false;
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    return generateParsingCode(field.getGenericType(), objVarName, parserPackage, field.getName());
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage, String fieldName) {
    return generateParsingCode(field.getGenericType(), objVarName, parserPackage, fieldName);
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

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("CustomObjectFieldParser cannot handle type: " + type.getTypeName());
    }
    String resultVarName = "level" + level + "Value"; // Consistent naming

    // Determine the correct parser class (could be generated or custom)
    // Pass the full Type to determineParserClassName
    ClassName parserClassName = ParserWriterUtils.determineParserClassName(type, parserPackage);

    // Generate the statement to declare the final variable and assign the result
    // of calling the specific object parser's parse method.
    // Assumes the accessExpression yields a JSONObjectHandle suitable for the target parser.
    code.addStatement("final $T $L = $T.parse($L)", type, resultVarName, parserClassName, accessExpression);

    return resultVarName;
  }
}