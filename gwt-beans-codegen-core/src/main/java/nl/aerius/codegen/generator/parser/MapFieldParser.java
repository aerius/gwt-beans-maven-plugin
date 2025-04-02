package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonKey;
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
    return canHandle(field.getType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType paramType = (ParameterizedType) type;
    return Map.class.isAssignableFrom((Class<?>) paramType.getRawType());
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
    if (!(type instanceof ParameterizedType)) {
      throw new IllegalArgumentException("MapFieldParser only handles ParameterizedType");
    }
    ParameterizedType paramType = (ParameterizedType) type;
    Type[] typeArgs = paramType.getActualTypeArguments();
    if (typeArgs.length != 2) {
      throw new IllegalArgumentException("MapFieldParser requires exactly 2 type arguments");
    }

    Type keyType = typeArgs[0];
    Type valueType = typeArgs[1];

    // Get the concrete map implementation
    Class<?> mapClass = (Class<?>) paramType.getRawType();
    ClassName mapImpl;
    if (mapClass.equals(Map.class)) {
      mapImpl = LINKED_HASH_MAP;
    } else {
      mapImpl = ClassName.get(mapClass);
    }

    return ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true, code -> {
      code.addStatement("final $T mapObj = $L.getObject($S)", ParserCommonUtils.getJSONObjectHandle(), objVarName, fieldName)
          .addStatement("final $T<$T, $T> map = new $T<>()", mapClass, keyType, valueType, mapImpl);

      // Handle key type
      if (keyType instanceof Class<?> && ((Class<?>) keyType).isEnum()) {
        code.add("mapObj.keySet().forEach(key -> {\n")
            .indent()
            .addStatement("map.put($T.valueOf(key), $L)", keyType, generateValueParsingCode(valueType, "mapObj", "key", parserPackage))
            .unindent()
            .addStatement("})");
      } else if (keyType.equals(Integer.class)) {
        code.add("mapObj.keySet().forEach(key -> {\n")
            .indent()
            .addStatement("map.put(Integer.parseInt(key), $L)", generateValueParsingCode(valueType, "mapObj", "key", parserPackage))
            .unindent()
            .addStatement("})");
      } else {
        code.add("mapObj.keySet().forEach(key -> {\n")
            .indent()
            .addStatement("map.put(key, $L)", generateValueParsingCode(valueType, "mapObj", "key", parserPackage))
            .unindent()
            .addStatement("})");
      }

      code.addStatement("config.set$L(map)", ParserCommonUtils.capitalize(fieldName));
    });
  }

  private CodeBlock generateValueParsingCode(Type valueType, String objVarName, String keyName, String parserPackage) {
    if (valueType instanceof Class<?>) {
      Class<?> clazz = (Class<?>) valueType;
      if (clazz.equals(String.class)) {
        return CodeBlock.of("$L.getString($L)", objVarName, keyName);
      } else if (clazz.equals(Integer.class)) {
        return CodeBlock.of("$L.getInteger($L)", objVarName, keyName);
      } else if (clazz.equals(Double.class)) {
        return CodeBlock.of("$L.getNumber($L)", objVarName, keyName);
      } else if (clazz.equals(Boolean.class)) {
        return CodeBlock.of("$L.getBoolean($L)", objVarName, keyName);
      } else if (clazz.isEnum()) {
        return CodeBlock.of("$T.valueOf($L.getString($L))", clazz, objVarName, keyName);
      } else if (clazz.equals(Map.class)) {
        return CodeBlock.of("$T.parse($L.getObject($L))", ParserWriterUtils.determineParserClassName(clazz, parserPackage), objVarName, keyName);
      } else if (clazz.equals(List.class)) {
        return CodeBlock.of("$T.parse($L.getObject($L))", ParserWriterUtils.determineParserClassName(clazz, parserPackage), objVarName, keyName);
      } else {
        return CodeBlock.of("$T.parse($L.getObject($L))", ParserWriterUtils.determineParserClassName(clazz, parserPackage), objVarName, keyName);
      }
    } else if (valueType instanceof java.lang.reflect.ParameterizedType) {
      return CodeBlock.of("$T.parse($L.getObject($S))", ParserWriterUtils.determineParserClassName(valueType, parserPackage), objVarName, keyName);
    } else {
      throw new IllegalArgumentException("Unsupported value type: " + valueType);
    }
  }

  private void generateCollectionValueMapCode(CodeBlock.Builder code, Type keyType, Type valueType,
      String objVarName, String parserPackage, String fieldName) {
    final ParameterizedType paramType = (ParameterizedType) valueType;
    final Type rawType = paramType.getRawType();
    final Type elementType = paramType.getActualTypeArguments()[0];

    // Skip if element type is a wildcard
    if (elementType.getTypeName().contains("?")) {
      code.add("// Skipping field with complex generic type: $L", valueType.getTypeName());
      return;
    }

    // Determine the map implementation
    final ClassName mapImpl = LINKED_HASH_MAP;

    final boolean isEnumKey = keyType instanceof Class<?> && ((Class<?>) keyType).isEnum();
    final ClassName keyClass = isEnumKey ? ClassName.get((Class<?>) keyType) : ParserCommonUtils.STRING;

    // Handle different collection types
    if (rawType.equals(List.class)) {
      if (elementType instanceof Class) {
        // Handle complex object types in lists
        final ClassName elementClass = ClassName.get((Class<?>) elementType);
        code.addStatement("final $T<$T, $T<$T>> map = new $T<>()",
            Map.class, keyClass, LIST, elementClass, mapImpl);

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
  }

  private boolean hasJsonKeyAnnotation(Class<?> clazz) {
    return clazz.getAnnotation(JsonKey.class) != null;
  }

  private String findJsonCreatorMethodName(Class<?> clazz) {
    for (Method method : clazz.getMethods()) {
      if (method.getAnnotation(JsonCreator.class) != null) {
        return method.getName();
      }
    }
    return null;
  }
}