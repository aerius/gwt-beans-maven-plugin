package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for Map fields.
 */
public class MapFieldParser implements FieldParser {
  // Map implementations
  private static final ClassName HASH_MAP = ClassName.get("java.util", "HashMap");
  private static final ClassName LINKED_HASH_MAP = ClassName.get("java.util", "LinkedHashMap");
  private static final ClassName LIST = ClassName.get("java.util", "List");
  private static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");

  @Override
  public boolean canHandle(Field field) {
    return Map.class.isAssignableFrom(field.getType());
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    final String fieldName = field.getName();

    try {
      final Type[] genericTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
      final Type keyType = genericTypes[0];
      final Type valueType = genericTypes[1];

      // Skip fields with wildcard types
      if (keyType.getTypeName().contains("?") || valueType.getTypeName().contains("?")) {
        final CodeBlock.Builder code = CodeBlock.builder();
        code.add("// Skipping field with complex generic type: $L", fieldName);
        return code.build();
      }

      final CodeBlock.Builder code = CodeBlock.builder();

      code.beginControlFlow(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true))
          .addStatement("final $T mapObj = $L.getObject($S)", ParserCommonUtils.getJSONObjectHandle(), objVarName,
              fieldName);

      // Handle interface types
      if (valueType.getTypeName().contains("Comparable")) {
        // For Comparable<?>, treat it as a map of string keys and values
        code.addStatement("final $T<$T, $T> map = new $T<>()", LINKED_HASH_MAP, ParserCommonUtils.STRING,
            ParserCommonUtils.STRING, LINKED_HASH_MAP)
            .add("mapObj.keySet().forEach(key -> {\n")
            .indent()
            .addStatement("map.put(key, mapObj.getString(key))")
            .unindent()
            .addStatement("})")
            .addStatement("config.set$L(map)", ParserCommonUtils.capitalize(fieldName))
            .endControlFlow();
        return code.build();
      }

      if (valueType instanceof ParameterizedType) {
        generateCollectionValueMapCode(code, field, keyType, valueType, parserPackage);
      } else {
        generateSimpleValueMapCode(code, keyType, valueType, field, objVarName, parserPackage);
      }

      code.endControlFlow();
      return code.build();
    } catch (ClassCastException e) {
      // Handle the case where we can't properly process the generic type
      final CodeBlock.Builder code = CodeBlock.builder();
      code.addStatement("// Skipping field with complex generic type: $L", field.getName());
      return code.build();
    }
  }

  private void generateSimpleValueMapCode(CodeBlock.Builder code, Type keyType, Type valueType, Field field,
      String objVarName, String parserPackage) {
    // Determine the map implementation based on the field's declared type
    Class<?> declaredType = field.getType();
    ClassName mapImpl;
    if (declaredType.equals(HashMap.class)) {
      mapImpl = HASH_MAP;
    } else {
      mapImpl = LINKED_HASH_MAP; // Default to LinkedHashMap for Map interface and LinkedHashMap
    }

    ClassName valueClass;
    String getterMethod;
    boolean isEnumKey = keyType instanceof Class<?> && ((Class<?>) keyType).isEnum();
    ClassName keyClass = isEnumKey ? ClassName.get((Class<?>) keyType) : ParserCommonUtils.STRING;

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

  private void generateCollectionValueMapCode(CodeBlock.Builder code, Field field, Type keyType, Type valueType,
      String parserPackage) {
    final String fieldName = field.getName();
    final ParameterizedType paramType = (ParameterizedType) valueType;
    final Type rawType = paramType.getRawType();
    final Type elementType = paramType.getActualTypeArguments()[0];

    // Skip if element type is a wildcard
    if (elementType.getTypeName().contains("?")) {
      code.add("// Skipping field with complex generic type: $L", fieldName);
      return;
    }

    // Determine the map implementation based on the field's declared type
    Class<?> declaredType = field.getType();
    ClassName mapImpl;
    if (declaredType.equals(HashMap.class)) {
      mapImpl = HASH_MAP;
    } else {
      mapImpl = LINKED_HASH_MAP; // Default to LinkedHashMap for Map interface and LinkedHashMap
    }

    boolean isEnumKey = keyType instanceof Class<?> && ((Class<?>) keyType).isEnum();
    ClassName keyClass = isEnumKey ? ClassName.get((Class<?>) keyType) : ParserCommonUtils.STRING;

    // Handle different collection types
    if (rawType.equals(List.class)) {
      if (elementType.equals(Double.class)) {
        code.addStatement("final $T<$T, $T<$T>> map = new $T<>()",
            mapImpl, keyClass, LIST, ClassName.get(Double.class), mapImpl);

        if (isEnumKey) {
          code.add("mapObj.keySet().forEach(key -> {\n")
              .indent()
              .addStatement("final $T enumKey = $T.valueOf(key)", keyClass, keyClass)
              .addStatement("map.put(enumKey, mapObj.getNumberArray(key))")
              .unindent()
              .addStatement("})");
        } else {
          code.add("mapObj.keySet().forEach(key -> {\n")
              .indent()
              .addStatement("map.put(key, mapObj.getNumberArray(key))")
              .unindent()
              .addStatement("})");
        }
      } else if (elementType.equals(Integer.class)) {
        code.addStatement("final $T<$T, $T<$T>> map = new $T<>()",
            mapImpl, keyClass, LIST, ClassName.get(Integer.class), mapImpl);

        if (isEnumKey) {
          code.add("mapObj.keySet().forEach(key -> {\n")
              .indent()
              .addStatement("final $T enumKey = $T.valueOf(key)", keyClass, keyClass)
              .addStatement("map.put(enumKey, mapObj.getIntegerArray(key))")
              .unindent()
              .addStatement("})");
        } else {
          code.add("mapObj.keySet().forEach(key -> {\n")
              .indent()
              .addStatement("map.put(key, mapObj.getIntegerArray(key))")
              .unindent()
              .addStatement("})");
        }
      } else if (elementType.equals(String.class)) {
        code.addStatement("final $T<$T, $T<$T>> map = new $T<>()",
            mapImpl, keyClass, LIST, ParserCommonUtils.STRING, mapImpl);

        if (isEnumKey) {
          code.add("mapObj.keySet().forEach(key -> {\n")
              .indent()
              .addStatement("final $T enumKey = $T.valueOf(key)", keyClass, keyClass)
              .addStatement("map.put(enumKey, mapObj.getStringArray(key))")
              .unindent()
              .addStatement("})");
        } else {
          code.add("mapObj.keySet().forEach(key -> {\n")
              .indent()
              .addStatement("map.put(key, mapObj.getStringArray(key))")
              .unindent()
              .addStatement("})");
        }
      } else if (elementType instanceof Class) {
        // Handle complex object types in lists
        ClassName elementClass = ClassName.get((Class<?>) elementType);
        code.addStatement("final $T<$T, $T<$T>> map = new $T<>()",
            mapImpl, keyClass, LIST, elementClass, mapImpl);

        if (isEnumKey) {
          code.add("mapObj.keySet().forEach(key -> {\n")
              .indent()
              .addStatement("final $T enumKey = $T.valueOf(key)", keyClass, keyClass)
              .addStatement("final $T<$T> list = new $T<>()", LIST, elementClass, ARRAY_LIST)
              .add("mapObj.getArray(key).forEach(item -> {\n")
              .indent()
              .addStatement("list.add($T.parse(item))",
                  ParserWriterUtils.determineParserClassName(((Class<?>) elementType).getSimpleName(), parserPackage))
              .unindent()
              .addStatement("})")
              .addStatement("map.put(enumKey, list)")
              .unindent()
              .addStatement("})");
        } else {
          code.add("mapObj.keySet().forEach(key -> {\n")
              .indent()
              .addStatement("final $T<$T> list = new $T<>()", LIST, elementClass, ARRAY_LIST)
              .add("mapObj.getArray(key).forEach(item -> {\n")
              .indent()
              .addStatement("list.add($T.parse(item))",
                  ParserWriterUtils.determineParserClassName(((Class<?>) elementType).getSimpleName(), parserPackage))
              .unindent()
              .addStatement("})")
              .addStatement("map.put(key, list)")
              .unindent()
              .addStatement("})");
        }
      } else {
        code.addStatement("// Unsupported collection element type: $L", elementType.getTypeName());
        return;
      }
    } else {
      code.addStatement("// Unsupported collection type: $L", rawType.getTypeName());
      return;
    }

    code.addStatement("config.set$L(map)", ParserCommonUtils.capitalize(fieldName));
  }
}