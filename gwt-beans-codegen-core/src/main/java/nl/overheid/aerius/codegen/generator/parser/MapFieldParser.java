package nl.overheid.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
      generateSimpleValueMapCode(code, keyType, valueType, objVarName, fieldName, parserPackage);
    }

    code.endControlFlow();
    return code.build();
  }

  private void generateSimpleValueMapCode(CodeBlock.Builder code, Type keyType, Type valueType, String objVarName,
      String fieldName, String parserPackage) {
    ClassName mapImpl;
    ClassName valueClass;
    String getterMethod;
    boolean isEnumKey = keyType instanceof Class<?> && ((Class<?>) keyType).isEnum();
    ClassName keyClass = isEnumKey ? ClassName.get((Class<?>) keyType) : ParserCommonUtils.STRING;

    if (valueType.equals(Integer.class)) {
      mapImpl = LINKED_HASH_MAP;
      valueClass = ClassName.get(Integer.class);
      getterMethod = "getInteger";
    } else if (valueType.equals(Double.class)) {
      mapImpl = LINKED_HASH_MAP;
      valueClass = ClassName.get(Double.class);
      getterMethod = "getNumber";
    } else if (valueType.equals(String.class)) {
      mapImpl = LINKED_HASH_MAP;
      valueClass = ParserCommonUtils.STRING;
      getterMethod = "getString";
    } else if (valueType instanceof Class<?> && !((Class<?>) valueType).isPrimitive()) {
      // Handle custom object types
      mapImpl = LINKED_HASH_MAP;
      valueClass = ClassName.get((Class<?>) valueType);
      code.addStatement("final $T<$T, $T> map = new $T<>()", mapImpl, keyClass, valueClass, mapImpl)
          .beginControlFlow("mapObj.keySet().forEach(key ->")
          .addStatement("final $T valueObj = mapObj.getObject(key)", ParserCommonUtils.getJSONObjectHandle());

      if (isEnumKey) {
        code.addStatement("final $T enumKey = $T.valueOf(key)", keyClass, keyClass)
            .addStatement("map.put(enumKey, $T.parse(valueObj))",
                ParserWriterUtils.determineParserClassName(((Class<?>) valueType).getSimpleName(), parserPackage));
      } else {
        code.addStatement("map.put(key, $T.parse(valueObj))",
            ParserWriterUtils.determineParserClassName(((Class<?>) valueType).getSimpleName(), parserPackage));
      }

      code.add("}")
          .add(")")
          .add(";\n")
          .addStatement("config.set$L(map)", ParserCommonUtils.capitalize(fieldName));
      return;
    } else {
      code.addStatement("// Unsupported map value type: $L", valueType.getTypeName());
      return;
    }

    code.addStatement("final $T<$T, $T> map = new $T<>()", mapImpl, keyClass, valueClass, mapImpl);

    if (isEnumKey) {
      code.beginControlFlow("mapObj.keySet().forEach(key ->")
          .addStatement("final $T enumKey = $T.valueOf(key)", keyClass, keyClass)
          .addStatement("map.put(enumKey, mapObj.$L(key))", getterMethod)
          .add("}")
          .add(")")
          .add(";\n");
    } else {
      code.addStatement("mapObj.keySet().forEach(key -> map.put(key, mapObj.$L(key)))", getterMethod);
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