package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

/**
 * Parser for primitive array fields (int[], byte[], etc.).
 */
public class PrimitiveArrayFieldParser implements TypeParser {
  private static final ClassName LIST = ClassName.get("java.util", "List");

  // Map primitive component types to the specific getter method on JSONArrayHandle
  private static final Map<Class<?>, String> PRIMITIVE_COMPONENT_TO_GETTER = new HashMap<>();

  static {
    // Assumes JSONArrayHandle has methods like getStringArray(), getIntegerArray() etc.
    // Adjust method names if they differ in nl.aerius.wui.service.json.JSONArrayHandle
    PRIMITIVE_COMPONENT_TO_GETTER.put(int.class, "getIntegerArray");
    PRIMITIVE_COMPONENT_TO_GETTER.put(long.class, "getLongArray"); // Assuming getLongArray exists
    PRIMITIVE_COMPONENT_TO_GETTER.put(double.class, "getNumberArray");
    PRIMITIVE_COMPONENT_TO_GETTER.put(boolean.class, "getBooleanArray");
    // Note: byte[], short[], float[], char[] might not have direct getters
    // and may require iterating and casting, similar to non-primitive arrays.
    // If they DO have direct getters (e.g., getByteArray), add them here.
    // For now, we only handle int[], long[], double[], boolean[].
  }

  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getGenericType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof Class<?>)) {
      return false;
    }
    Class<?> clazz = (Class<?>) type;
    return clazz.isArray() && PRIMITIVE_COMPONENT_TO_GETTER.containsKey(clazz.getComponentType());
    // Only handle primitives with direct getters for simplicity now.
    // return clazz.isArray() && clazz.getComponentType().isPrimitive();
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
      throw new IllegalArgumentException("PrimitiveArrayFieldParser only handles Class types");
    }
    Class<?> clazz = (Class<?>) type;
    if (!clazz.isArray()) {
      throw new IllegalArgumentException("PrimitiveArrayFieldParser only handles array types");
    }

    Class<?> componentType = clazz.getComponentType();
    final CodeBlock.Builder code = CodeBlock.builder();

    code.beginControlFlow("if ($L.has($S) && !$L.isNull($S))", objVarName, fieldName, objVarName, fieldName);

    if (componentType.equals(String.class)) {
      code.addStatement("config.set$L($L.getStringArray($S))", ParserCommonUtils.capitalize(fieldName), objVarName, fieldName);
    } else if (componentType.equals(Integer.class)) {
      code.addStatement("config.set$L($L.getIntegerArray($S))", ParserCommonUtils.capitalize(fieldName), objVarName, fieldName);
    } else if (componentType.equals(Double.class)) {
      code.addStatement("config.set$L($L.getNumberArray($S))", ParserCommonUtils.capitalize(fieldName), objVarName, fieldName);
    } else if (componentType.equals(Boolean.class)) {
      code.addStatement("config.set$L($L.getBooleanArray($S))", ParserCommonUtils.capitalize(fieldName), objVarName, fieldName);
    } else {
      throw new IllegalArgumentException("Unsupported array component type: " + componentType.getName());
    }

    code.endControlFlow();
    return code.build();
  }

  // --- New Recursive Parsing Method ---
  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    return generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, type);
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level, Type fieldType) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("PrimitiveArrayFieldParser cannot handle type: " + type.getTypeName());
    }
    // fieldType not needed for declaration, use runtime type
    Class<?> arrayType = (Class<?>) type;
    Class<?> componentType = arrayType.getComponentType();
    String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "Array");
    String getterMethod = PRIMITIVE_COMPONENT_TO_GETTER.get(componentType);

    if (getterMethod == null) {
      // Fallback or error for unsupported primitive array types
      code.addStatement("$T[] $L = null; // Unsupported primitive array type", componentType, resultVarName);
    } else {
      code.addStatement("final $T[] $L = $L.$L($L)", componentType, resultVarName, objVarName, getterMethod, accessExpression);
    }
    return resultVarName;
  }
}