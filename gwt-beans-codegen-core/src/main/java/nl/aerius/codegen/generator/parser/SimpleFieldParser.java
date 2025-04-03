package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.palantir.javapoet.CodeBlock;

/**
 * Parser for simple field types like primitives and their wrappers.
 */
public class SimpleFieldParser implements TypeParser {
  private static final Map<Class<?>, String> TYPE_TO_GETTER_METHOD = new HashMap<>();
  private static final Map<Class<?>, Boolean> TYPE_IS_WRAPPER = new HashMap<>();

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

    // Initialize wrapper type mapping
    TYPE_IS_WRAPPER.put(String.class, true);
    TYPE_IS_WRAPPER.put(int.class, false);
    TYPE_IS_WRAPPER.put(Integer.class, true);
    TYPE_IS_WRAPPER.put(long.class, false);
    TYPE_IS_WRAPPER.put(Long.class, true);
    TYPE_IS_WRAPPER.put(double.class, false);
    TYPE_IS_WRAPPER.put(Double.class, true);
    TYPE_IS_WRAPPER.put(boolean.class, false);
    TYPE_IS_WRAPPER.put(Boolean.class, true);
    TYPE_IS_WRAPPER.put(byte.class, false);
    TYPE_IS_WRAPPER.put(Byte.class, true);
    TYPE_IS_WRAPPER.put(short.class, false);
    TYPE_IS_WRAPPER.put(Short.class, true);
    TYPE_IS_WRAPPER.put(float.class, false);
    TYPE_IS_WRAPPER.put(Float.class, true);
    TYPE_IS_WRAPPER.put(char.class, false);
    TYPE_IS_WRAPPER.put(Character.class, true);
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
    return TYPE_TO_GETTER_METHOD.containsKey(clazz) &&
        !clazz.isArray() &&
        !clazz.isEnum();
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
      throw new IllegalArgumentException("SimpleFieldParser only handles Class types");
    }
    Class<?> clazz = (Class<?>) type;

    String getterMethod = TYPE_TO_GETTER_METHOD.get(clazz);
    boolean isWrapper = TYPE_IS_WRAPPER.getOrDefault(clazz, false);

    return ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, isWrapper, code -> {
      addSimpleFieldSetter(code, objVarName, fieldName, getterMethod, isWrapper, clazz);
    });
  }

  private void addSimpleFieldSetter(CodeBlock.Builder code, String objVarName, String fieldName,
      String getterMethod, boolean isWrapper, Class<?> fieldType) {
    if (getterMethod.equals("getInteger")) {
      if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
        code.addStatement("config.set$L((byte) $L.$L($S))", ParserCommonUtils.capitalize(fieldName), objVarName,
            getterMethod,
            fieldName);
      } else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
        code.addStatement("config.set$L((short) $L.$L($S))", ParserCommonUtils.capitalize(fieldName), objVarName,
            getterMethod,
            fieldName);
      } else {
        code.addStatement("config.set$L($L.$L($S))", ParserCommonUtils.capitalize(fieldName), objVarName, getterMethod,
            fieldName);
      }
    } else if (getterMethod.equals("getString")) {
      if (fieldType.equals(char.class) || fieldType.equals(Character.class)) {
        code.addStatement("String charStr = $L.$L($S)", objVarName, getterMethod, fieldName)
            .beginControlFlow("if (charStr != null && !charStr.isEmpty())")
            .addStatement("config.set$L(charStr.charAt(0))", ParserCommonUtils.capitalize(fieldName))
            .endControlFlow();
      } else {
        code.addStatement("config.set$L($L.$L($S))", ParserCommonUtils.capitalize(fieldName), objVarName, getterMethod,
            fieldName);
      }
    } else if (getterMethod.equals("getNumber")) {
      if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
        code.addStatement("config.set$L($L.$L($S).floatValue())", ParserCommonUtils.capitalize(fieldName), objVarName,
            getterMethod,
            fieldName);
      } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
        code.addStatement("config.set$L($L.$L($S).intValue())", ParserCommonUtils.capitalize(fieldName), objVarName,
            getterMethod,
            fieldName);
      } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
        code.addStatement("config.set$L($L.$L($S).longValue())", ParserCommonUtils.capitalize(fieldName), objVarName,
            getterMethod,
            fieldName);
      } else {
        // For double, we can use the number directly
        code.addStatement("config.set$L($L.$L($S))", ParserCommonUtils.capitalize(fieldName), objVarName,
            getterMethod,
            fieldName);
      }
    } else {
      code.addStatement("config.set$L($L.$L($S))", ParserCommonUtils.capitalize(fieldName), objVarName, getterMethod,
          fieldName);
    }
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("SimpleFieldParser cannot handle type: " + type.getTypeName());
    }
    Class<?> clazz = (Class<?>) type;
    String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "value");
    String tempStringVar = ParserCommonUtils.getVariableNameForLevel(level, "str");

    if (clazz.equals(char.class) || clazz.equals(Character.class)) {
      // Special handling for char/Character
      code.addStatement("final String $L = $L", tempStringVar, accessExpression); // accessExpression should yield String here
      // Declare the final char variable, initialized carefully
      if (clazz.equals(Character.class)) {
        code.addStatement("final $T $L = ($L != null && !$L.isEmpty()) ? $L.charAt(0) : null",
            clazz, resultVarName, tempStringVar, tempStringVar, tempStringVar);
      } else { // char primitive
        // Use integer 0 as the default, as char literal rendering seems problematic
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