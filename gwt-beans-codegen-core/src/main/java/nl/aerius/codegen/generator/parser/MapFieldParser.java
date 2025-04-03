package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for Map fields.
 */
public class MapFieldParser implements TypeParser {
  // Map implementations
  private static final ClassName HASH_MAP = ClassName.get("java.util", "HashMap");
  private static final ClassName LINKED_HASH_MAP = ClassName.get("java.util", "LinkedHashMap");
  private static final ClassName LIST = ClassName.get("java.util", "List");
  private static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");

  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getGenericType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType paramType = (ParameterizedType) type;
    if (!(paramType.getRawType() instanceof Class<?>)) {
      return false;
    }
    return Map.class.isAssignableFrom((Class<?>) paramType.getRawType());
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
    return generateParsingCode(type, objVarName, parserPackage, null);
  }

  @Override
  public CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage, String fieldName) {
    CodeBlock.Builder code = CodeBlock.builder();
    CodeBlock accessExpression;
    if (fieldName != null) {
      accessExpression = ParserCommonUtils.createFieldAccessCode(type, objVarName, CodeBlock.of("$S", fieldName));
      code.add(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true, innerCode -> {
        String resultVar = generateParsingCodeInto(innerCode, type, objVarName, parserPackage, accessExpression, 1);
        innerCode.addStatement("// Assign the result: config.set$L($L);", ParserCommonUtils.capitalize(fieldName), resultVar);
      }));
    } else {
      accessExpression = CodeBlock.of("$L", objVarName);
      generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, 1);
    }
    return code.build();
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("MapFieldParser cannot handle type: " + type.getTypeName());
    }
    ParameterizedType mapType = (ParameterizedType) type;
    Type keyType = mapType.getActualTypeArguments()[0];
    Type valueType = mapType.getActualTypeArguments()[1];
    ClassName mapImpl = ClassName.get(java.util.LinkedHashMap.class);

    String mapVar = ParserCommonUtils.getVariableNameForLevel(level, "Map");
    String objVar = ParserCommonUtils.getVariableNameForLevel(level, "Obj");
    String keyVar = ParserCommonUtils.getVariableNameForLevel(level, "Key");

    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONObjectHandle(), objVar, accessExpression);

    code.addStatement("final $T<$T, $T> $L = new $T<>()", Map.class, keyType, valueType, mapVar, mapImpl);

    code.add("$L.keySet().forEach($L -> {\n", objVar, keyVar)
        .indent();

    CodeBlock valueAccessExpression = ParserCommonUtils.createFieldAccessCode(
        valueType,
        objVar,
        CodeBlock.of("$L", keyVar));

    String valueVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
        code,
        valueType,
        objVar,
            parserPackage,
        valueAccessExpression,
        level + 1);

    addPutStatement(code, mapVar, keyType, keyVar, valueVarName);

    code.unindent()
        .addStatement("})");

    return mapVar;
  }

  private void addPutStatement(CodeBlock.Builder code, String mapVar, Type keyType, String keyVar, String valueVar) {
    CodeBlock keyExpression = CodeBlock.of("$L", keyVar);
    if (keyType.equals(Integer.class)) {
      keyExpression = CodeBlock.of("Integer.parseInt($L)", keyVar);
    } else if (keyType instanceof Class<?> && ((Class<?>) keyType).isEnum()) {
      keyExpression = CodeBlock.of("$T.valueOf($L)", keyType, keyVar);
    }

    code.addStatement("$L.put($L, $L)", mapVar, keyExpression, valueVar);
  }
}