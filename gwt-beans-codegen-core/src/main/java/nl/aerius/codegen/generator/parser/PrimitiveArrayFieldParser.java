package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

/**
 * Parser for primitive array fields (int[], byte[], etc.).
 */
public class PrimitiveArrayFieldParser implements TypeParser {
  private static final ClassName LIST = ClassName.get("java.util", "List");

  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof Class<?>)) {
      return false;
    }
    Class<?> clazz = (Class<?>) type;
    return clazz.isArray() && clazz.getComponentType().isPrimitive();
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
}