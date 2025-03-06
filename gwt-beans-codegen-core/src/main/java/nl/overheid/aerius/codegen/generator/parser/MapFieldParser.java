package nl.overheid.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.overheid.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for Map fields.
 */
public class MapFieldParser implements FieldParser {
  // Map implementations
  private static final ClassName HASH_MAP = ClassName.get("java.util", "HashMap");
  private static final ClassName LINKED_HASH_MAP = ClassName.get("java.util", "LinkedHashMap");

  @Override
  public boolean canHandle(Field field) {
    return Map.class.isAssignableFrom(field.getType());
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    final String fieldName = field.getName();
    final Type[] genericTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
    final Type keyType = genericTypes[0];
    final Type valueType = genericTypes[1];

    final CodeBlock.Builder code = CodeBlock.builder();

    code.beginControlFlow(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true))
        .addStatement("final $T mapObj = $L.getObject($S)", ParserCommonUtils.getJSONObjectHandle(), objVarName,
            fieldName);

    if (valueType instanceof ParameterizedType) {
      generateCollectionValueMapCode(code, valueType, objVarName);
    } else {
      generateSimpleValueMapCode(code, keyType, valueType, field, objVarName, parserPackage);
    }

    code.endControlFlow();
    return code.build();
  }

  private void generateSimpleValueMapCode(CodeBlock.Builder code, Type keyType, Type valueType, Field field,
      String objVarName, String parserPackage) {
    ClassName mapImpl;
    ClassName valueClass;
    String getterMethod;
    boolean isEnumKey = keyType instanceof Class<?> && ((Class<?>) keyType).isEnum();
    ClassName keyClass = isEnumKey ? ClassName.get((Class<?>) keyType) : ParserCommonUtils.STRING;

    // Determine the map implementation based on the field's declared type
    Class<?> declaredType = field.getType();
    if (declaredType.equals(HashMap.class)) {
      mapImpl = HASH_MAP;
    } else {
      mapImpl = LINKED_HASH_MAP; // Default to LinkedHashMap for Map interface and LinkedHashMap
    }

    String fieldName = field.getName();

    if (valueType.equals(Integer.class)) {
      valueClass = ClassName.get(Integer.class);
      getterMethod = "getInteger";
    } else if (valueType.equals(Double.class)) {
      valueClass = ClassName.get(Double.class);
      getterMethod = "getNumber";
    } else if (valueType.equals(String.class)) {
      valueClass = ParserCommonUtils.STRING;
      getterMethod = "getString";
    } else {
      // Handle custom object types
      valueClass = ClassName.get((Class<?>) valueType);
      code.addStatement("final $T<$T, $T> map = new $T<>()", mapImpl, keyClass, valueClass, mapImpl)
          .add("mapObj.keySet().forEach(key -> {\n")
          .indent()
          .addStatement("final $T valueObj = mapObj.getObject(key)", ParserCommonUtils.getJSONObjectHandle());

      if (isEnumKey) {
        code.addStatement("final $T enumKey = $T.valueOf(key)", keyClass, keyClass)
            .addStatement("map.put(enumKey, $T.parse(valueObj))",
                ParserWriterUtils.determineParserClassName(((Class<?>) valueType).getSimpleName(), parserPackage));
      } else {
        code.addStatement("map.put(key, $T.parse(valueObj))",
            ParserWriterUtils.determineParserClassName(((Class<?>) valueType).getSimpleName(), parserPackage));
      }

      code.unindent();
      code.addStatement("})")
          .addStatement("config.set$L(map)", ParserCommonUtils.capitalize(fieldName));
      return;
    }

    code.addStatement("final $T<$T, $T> map = new $T<>()", mapImpl, keyClass, valueClass, mapImpl);

    if (isEnumKey) {
      code.add("mapObj.keySet().forEach(key -> {\n")
          .indent()
          .addStatement("final $T enumKey = $T.valueOf(key)", keyClass, keyClass)
          .addStatement("map.put(enumKey, mapObj.$L(key))", getterMethod)
          .unindent()
          .addStatement("})");
    } else {
      code.add("mapObj.keySet().forEach(key -> {\n")
          .indent()
          .addStatement("map.put(key, mapObj.$L(key))", getterMethod)
          .unindent()
          .addStatement("})");
    }

    code.addStatement("config.set$L(map)", ParserCommonUtils.capitalize(fieldName));
  }

  private void generateCollectionValueMapCode(CodeBlock.Builder code, Type valueType, String objVarName) {
    final ParameterizedType paramType = (ParameterizedType) valueType;
    final Type elementType = paramType.getActualTypeArguments()[0];
    String getterMethod;

    if (elementType.equals(Double.class)) {
      getterMethod = "getNumberArray";
    } else if (elementType.equals(Integer.class)) {
      getterMethod = "getIntegerArray";
    } else if (elementType.equals(String.class)) {
      getterMethod = "getStringArray";
    } else {
      code.addStatement("// Unsupported collection element type: $L", elementType.getTypeName());
      return;
    }

    code.addStatement("mapObj.keySet().forEach(key -> map.put(key, mapObj.$L(key)))", getterMethod);
  }
}