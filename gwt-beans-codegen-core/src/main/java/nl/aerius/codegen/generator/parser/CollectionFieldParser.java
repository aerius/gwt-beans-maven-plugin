package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for Collection fields (List, Set, etc.) and Object arrays.
 */
public class CollectionFieldParser implements TypeParser {
  // Collection type implementations
  private static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");
  private static final ClassName HASH_SET = ClassName.get("java.util", "HashSet");

  // Map of element types to their array getter methods
  private static final Map<Type, String> ELEMENT_TYPE_TO_ARRAY_GETTER = new HashMap<>();

  static {
    // Initialize element type to array getter method mapping
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(String.class, "getStringArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Integer.class, "getIntegerArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Double.class, "getNumberArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Boolean.class, "getBooleanArray");
  }

  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (type instanceof Class<?>) {
      Class<?> clazz = (Class<?>) type;
      return Collection.class.isAssignableFrom(clazz) ||
          (clazz.isArray() && !clazz.getComponentType().isPrimitive());
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) type;
      return Collection.class.isAssignableFrom((Class<?>) paramType.getRawType());
    }
    return false;
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
    if (!(type instanceof java.lang.reflect.ParameterizedType)) {
      throw new IllegalArgumentException("CollectionFieldParser only handles ParameterizedType");
    }

    java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) type;
    Type[] typeArgs = paramType.getActualTypeArguments();
    if (typeArgs.length != 1) {
      throw new IllegalArgumentException("CollectionFieldParser requires exactly 1 type argument");
    }

    Type elementType = typeArgs[0];

    return ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true, code -> {
      generateCollectionParsingCode(code, type, objVarName, fieldName, parserPackage);
    });
  }

  private void generateCollectionParsingCode(CodeBlock.Builder code, Type type, String objVarName,
      String fieldName, String parserPackage) {
    if (!(type instanceof ParameterizedType)) {
      throw new IllegalArgumentException("CollectionFieldParser only handles ParameterizedType for collections");
    }
    ParameterizedType paramType = (ParameterizedType) type;
    Type rawType = paramType.getRawType();
    Type elementType = paramType.getActualTypeArguments()[0];

    // Skip if element type is a wildcard
    if (elementType.getTypeName().contains("?")) {
      code.add("// Skipping field with complex generic type: $L", type.getTypeName());
      return;
    }

    // Determine the appropriate collection implementation
    ClassName collectionImpl;
    if (rawType.equals(HashSet.class) || rawType.equals(Set.class)) {
      collectionImpl = HASH_SET;
    } else {
      collectionImpl = ARRAY_LIST;
    }

    // Check if we have a direct getter method for this element type
    if (ELEMENT_TYPE_TO_ARRAY_GETTER.containsKey(elementType)) {
      String getterMethod = ELEMENT_TYPE_TO_ARRAY_GETTER.get(elementType);
      code.addStatement("config.set$L(new $T<>($L.$L($S)))",
          ParserCommonUtils.capitalize(fieldName),
          collectionImpl,
          objVarName,
          getterMethod,
          fieldName);
    } else if (elementType instanceof Class<?> && ((Class<?>) elementType).isEnum()) {
      // Handle List<Enum> - using forEachString with valueOf
      final ClassName enumType;
      Class<?> enumClass = (Class<?>) elementType;
      if (enumClass.isMemberClass()) {
        // For inner enums, we need to include the enclosing class
        Class<?> enclosingClass = enumClass.getEnclosingClass();
        enumType = ClassName.get(enclosingClass.getPackage().getName(),
            enclosingClass.getSimpleName(),
            enumClass.getSimpleName());
      } else {
        enumType = ClassName.get(enumClass);
      }

      code.addStatement("final $T<$T> $L = new $T<>()", rawType, enumType, fieldName, collectionImpl)
          .add("$L.getArray($S).forEachString(str -> {\n", objVarName, fieldName)
          .indent()
          .addStatement("$L.add($T.valueOf(str))", fieldName, enumType)
          .unindent()
          .addStatement("})")
          .addStatement("config.set$L($L)", ParserCommonUtils.capitalize(fieldName), fieldName);
    } else if (elementType instanceof Class<?>) {
      // Handle List<CustomObject> - using forEach with parse
      Class<?> elementClass = (Class<?>) elementType;
      code.addStatement("final $T<$T> $L = new $T<>()", rawType, elementClass, fieldName, collectionImpl)
          .add("$L.getArray($S).forEach(item -> {\n", objVarName, fieldName)
          .indent()
          .addStatement("$L.add($T.parse(item))", fieldName,
              ParserWriterUtils.determineParserClassName(elementClass.getSimpleName(), parserPackage))
          .unindent()
          .addStatement("})")
          .addStatement("config.set$L($L)", ParserCommonUtils.capitalize(fieldName), fieldName);
    } else {
      code.addStatement("// Unsupported collection element type: $L", elementType.getTypeName());
    }
  }

  private void generateArrayParsingCode(CodeBlock.Builder code, String objVarName, String fieldName,
          Class<?> componentType) {
    // Check if we have a direct getter method for this component type
    if (ELEMENT_TYPE_TO_ARRAY_GETTER.containsKey(componentType)) {
      String getterMethod = ELEMENT_TYPE_TO_ARRAY_GETTER.get(componentType);
      code.addStatement("config.set$L($L.$L($S))",
          ParserCommonUtils.capitalize(fieldName),
          objVarName,
          getterMethod,
          fieldName);
    } else if (componentType.isEnum()) {
      // Handle Enum[] - using forEachString with valueOf
      final ClassName enumType;
      if (componentType.isMemberClass()) {
        // For inner enums, we need to include the enclosing class
        Class<?> enclosingClass = componentType.getEnclosingClass();
        enumType = ClassName.get(enclosingClass.getPackage().getName(),
            enclosingClass.getSimpleName(),
            componentType.getSimpleName());
      } else {
        enumType = ClassName.get(componentType);
      }

      code.addStatement("final $T[] $L = new $T[$L.getArray($S).length()]",
          componentType, fieldName, componentType, objVarName, fieldName)
          .add("$L.getArray($S).forEachString((str, index) -> {\n", objVarName, fieldName)
          .indent()
          .addStatement("$L[index] = $T.valueOf(str)", fieldName, enumType)
          .unindent()
          .addStatement("})")
          .addStatement("config.set$L($L)", ParserCommonUtils.capitalize(fieldName), fieldName);
    } else {
      // Handle CustomObject[] - using forEach with parse
      code.addStatement("final $T[] $L = new $T[$L.getArray($S).length()]",
          componentType, fieldName, componentType, objVarName, fieldName)
          .add("$L.getArray($S).forEach((item, index) -> {\n", objVarName, fieldName)
          .indent()
          .addStatement("$L[index] = $T.parse(item)", fieldName,
              ParserWriterUtils.determineParserClassName(componentType.getSimpleName(), "nl.aerius.codegen.generator"))
          .unindent()
          .addStatement("})")
          .addStatement("config.set$L($L)", ParserCommonUtils.capitalize(fieldName), fieldName);
    }
  }
}