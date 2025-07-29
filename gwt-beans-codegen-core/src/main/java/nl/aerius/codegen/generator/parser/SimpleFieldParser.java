package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.palantir.javapoet.CodeBlock;

/**
 * Parser for simple field types like primitives and their wrappers.
 */
public class SimpleFieldParser implements TypeParser {
  private static final Map<Class<?>, String> TYPE_TO_GETTER_METHOD = new HashMap<>();

  static {
    // Initialize type to getter method mapping
    TYPE_TO_GETTER_METHOD.put(String.class, "getString");
    TYPE_TO_GETTER_METHOD.put(int.class, "getInteger");
    TYPE_TO_GETTER_METHOD.put(Integer.class, "getInteger");
    TYPE_TO_GETTER_METHOD.put(long.class, "getLong");
    TYPE_TO_GETTER_METHOD.put(Long.class, "getLong");
    TYPE_TO_GETTER_METHOD.put(double.class, "getNumber");
    TYPE_TO_GETTER_METHOD.put(Double.class, "getNumber");
    TYPE_TO_GETTER_METHOD.put(boolean.class, "getBoolean");
    TYPE_TO_GETTER_METHOD.put(Boolean.class, "getBoolean");
    TYPE_TO_GETTER_METHOD.put(byte.class, "getInteger");
    TYPE_TO_GETTER_METHOD.put(Byte.class, "getInteger");
    TYPE_TO_GETTER_METHOD.put(short.class, "getInteger");
    TYPE_TO_GETTER_METHOD.put(Short.class, "getInteger");
    TYPE_TO_GETTER_METHOD.put(float.class, "getNumber");
    TYPE_TO_GETTER_METHOD.put(Float.class, "getNumber");
    TYPE_TO_GETTER_METHOD.put(char.class, "getString");
    TYPE_TO_GETTER_METHOD.put(Character.class, "getString");
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof Class<?>)) {
      return false;
    }
    Class<?> clazz = (Class<?>) type;
    return TYPE_TO_GETTER_METHOD.containsKey(clazz) &&
        !clazz.isArray() &&
        !clazz.isEnum();
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
      throw new IllegalArgumentException("SimpleFieldParser cannot handle type: " + type.getTypeName());
    }
    Class<?> clazz = (Class<?>) type;
    String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "value");
    String tempStringVar = ParserCommonUtils.getVariableNameForLevel(level, "str");

    if (clazz.equals(char.class) || clazz.equals(Character.class)) {
      // Special handling for char/Character
      code.addStatement("final String $L = $L", tempStringVar, accessExpression);
      if (clazz.equals(Character.class)) {
        code.addStatement("final $T $L = ($L != null && !$L.isEmpty()) ? $L.charAt(0) : null",
            clazz, resultVarName, tempStringVar, tempStringVar, tempStringVar);
      } else { // char primitive
        code.addStatement("final $T $L = ($L != null && !$L.isEmpty()) ? $L.charAt(0) : 0",
                clazz, resultVarName, tempStringVar, tempStringVar, tempStringVar);
      }
    } else {
      // Handle all other simple types using the helper
      CodeBlock assignment = createAssignmentExpression(clazz, accessExpression);
      code.addStatement("final $T $L = $L", type, resultVarName, assignment);
    }

    return resultVarName;
  }

  private CodeBlock createAssignmentExpression(Class<?> targetType, CodeBlock accessExpression) {
    if (targetType.equals(byte.class) || targetType.equals(Byte.class)) {
      return CodeBlock.of("(byte) $L", accessExpression);
    } else if (targetType.equals(short.class) || targetType.equals(Short.class)) {
      return CodeBlock.of("(short) $L", accessExpression);
    } else if (targetType.equals(float.class) || targetType.equals(Float.class)) {
      return CodeBlock.of("$L.floatValue()", accessExpression);
    } else {
      // String, Integer, Long, Double, Boolean
      return accessExpression;
    }
  }
}