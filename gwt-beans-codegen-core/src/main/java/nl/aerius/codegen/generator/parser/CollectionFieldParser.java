package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for Collection fields (List, Set, etc.) and Object arrays.
 */
public class CollectionFieldParser implements FieldParser {
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
    Class<?> fieldType = field.getType();
    return Collection.class.isAssignableFrom(fieldType) ||
        (fieldType.isArray() && !fieldType.getComponentType().isPrimitive());
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    final CodeBlock.Builder code = CodeBlock.builder();
    final String fieldName = field.getName();
    final Class<?> fieldType = field.getType();

    code.beginControlFlow(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true));

    if (fieldType.isArray()) {
      generateArrayParsingCode(code, field, objVarName, fieldType.getComponentType());
    } else {
      generateCollectionParsingCode(code, field, objVarName, fieldType, parserPackage);
    }

    code.endControlFlow();
    return code.build();
  }

  private void generateCollectionParsingCode(CodeBlock.Builder code, Field field, String objVarName,
      Class<?> fieldType, String parserPackage) {
    final String fieldName = field.getName();
    // Get the generic type argument (e.g., String from List<String>)
    final Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    final Class<?> elementType = (Class<?>) genericType;

    // Determine the appropriate collection implementation
    ClassName collectionImpl;
    if (fieldType.equals(HashSet.class) || fieldType.equals(Set.class)) {
      collectionImpl = HASH_SET;
    } else {
      collectionImpl = ARRAY_LIST;
    }

    // Check if we have a direct getter method for this element type
    if (ELEMENT_TYPE_TO_ARRAY_GETTER.containsKey(genericType)) {
      String getterMethod = ELEMENT_TYPE_TO_ARRAY_GETTER.get(genericType);
      code.addStatement("config.set$L(new $T<>($L.$L($S)))",
          ParserCommonUtils.capitalize(fieldName),
          collectionImpl,
          objVarName,
          getterMethod,
          fieldName);
    } else if (elementType.isEnum()) {
      // Handle List<Enum> - using forEachString with valueOf
      code.addStatement("final $T<$T> $L = new $T<>()", List.class, elementType, fieldName, ARRAY_LIST)
          .add("$L.getArray($S).forEachString(str -> {\n", objVarName, fieldName)
          .indent()
          .addStatement("$L.add($T.valueOf(str))", fieldName, elementType)
          .unindent()
          .addStatement("})")
          .addStatement("config.set$L($L)", ParserCommonUtils.capitalize(fieldName), fieldName);
    } else {
      // Handle complex element types that require custom parsing
      final String elementTypeName = ((Class<?>) genericType).getSimpleName();
      code.addStatement("final $T<$T> $L = new $T<>()", collectionImpl, genericType, fieldName + "List", collectionImpl)
          .addStatement("$L.getArray($S).forEach(element -> $L.add($T.parse(element)))", 
              objVarName, fieldName, fieldName + "List",
              ParserWriterUtils.determineParserClassName(elementTypeName, parserPackage))
          .addStatement("config.set$L($L)", ParserCommonUtils.capitalize(fieldName), fieldName + "List");
    }
  }

  private void generateArrayParsingCode(CodeBlock.Builder code, Field field, String objVarName,
      Class<?> componentType) {
    final String fieldName = field.getName();

    // Check if we have a direct getter method for this component type
    if (ELEMENT_TYPE_TO_ARRAY_GETTER.containsKey(componentType)) {
      String getterMethod = ELEMENT_TYPE_TO_ARRAY_GETTER.get(componentType);

      if (componentType.equals(String.class)) {
        code.addStatement("config.set$L($L.$L($S))",
            ParserCommonUtils.capitalize(fieldName),
            objVarName,
            getterMethod,
            fieldName);
      } else {
        code.addStatement("config.set$L($L.$L($S).toArray(new $T[0]))",
            ParserCommonUtils.capitalize(fieldName),
            objVarName,
            getterMethod,
            fieldName,
            componentType);
      }
    } else {
      code.addStatement("// Unsupported array component type: $L", componentType.getName());
    }
  }
}