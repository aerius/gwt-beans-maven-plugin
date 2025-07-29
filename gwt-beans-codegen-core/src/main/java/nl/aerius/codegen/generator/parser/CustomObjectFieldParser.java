package nl.aerius.codegen.generator.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

/**
 * Parser for custom object fields that require their own generated or custom parsers.
 */
public class CustomObjectFieldParser implements TypeParser {
  private final Map<String, String> customParserImports;

  public CustomObjectFieldParser(Map<String, String> customParserImports) {
    this.customParserImports = customParserImports;
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
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    return generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, type);
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level, Type fieldType) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("CustomObjectFieldParser cannot handle type: " + type.getTypeName());
    }

    Class<?> targetClass;
    if (type instanceof ParameterizedType) {
      Type rawType = ((ParameterizedType) type).getRawType();
      if (rawType instanceof Class<?>) {
        targetClass = (Class<?>) rawType;
      } else {
        throw new IllegalArgumentException("Cannot handle non-class raw type in ParameterizedType: " + rawType.getTypeName());
      }
    } else if (type instanceof Class<?>) {
      targetClass = (Class<?>) type;
    } else {
      throw new IllegalArgumentException("Unexpected type structure in CustomObjectFieldParser: " + type.getTypeName());
    }

    String parserSimpleName = targetClass.getSimpleName() + "Parser";
    String customParserFQN = customParserImports.get(parserSimpleName);
    ClassName parserClassName;

    if (customParserFQN != null) {
      parserClassName = ClassName.get(customParserFQN.substring(0, customParserFQN.lastIndexOf('.')), parserSimpleName);
    } else {
      parserClassName = ClassName.get(parserPackage, parserSimpleName);
    }

    String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "value");

    code.addStatement("final $T $L = $T.parse($L)", fieldType, resultVarName, parserClassName, accessExpression);

    return resultVarName;
  }
}