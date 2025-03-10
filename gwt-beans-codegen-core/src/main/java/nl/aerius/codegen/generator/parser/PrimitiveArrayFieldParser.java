package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

/**
 * Parser for primitive array fields (int[], byte[], etc.).
 */
public class PrimitiveArrayFieldParser implements FieldParser {
  private static final ClassName LIST = ClassName.get("java.util", "List");

  @Override
  public boolean canHandle(Field field) {
    Class<?> fieldType = field.getType();
    return fieldType.isArray() && fieldType.getComponentType().isPrimitive();
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    final CodeBlock.Builder code = CodeBlock.builder();
    final String fieldName = field.getName();
    final Class<?> componentType = field.getType().getComponentType();

    code.beginControlFlow(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true));

    if (componentType == int.class) {
      code.addStatement("final $T<$T> primitiveArray = $L.getIntegerArray($S)", LIST, Integer.class, objVarName,
          fieldName)
          .addStatement("config.set$L(primitiveArray.stream().mapToInt(Integer::intValue).toArray())",
              ParserCommonUtils.capitalize(fieldName));
    } else {
      code.addStatement("// Unsupported primitive array type: $L[]", componentType.getName());
    }

    code.endControlFlow();
    return code.build();
  }
}