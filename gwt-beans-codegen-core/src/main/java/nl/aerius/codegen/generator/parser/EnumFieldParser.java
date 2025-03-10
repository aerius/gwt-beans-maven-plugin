package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

/**
 * Parser for enum fields.
 */
public class EnumFieldParser implements FieldParser {

  @Override
  public boolean canHandle(Field field) {
    return field.getType().isEnum();
  }

  @Override
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    final CodeBlock.Builder code = CodeBlock.builder();
    final String fieldName = field.getName();
    final Class<?> fieldType = field.getType();
    final String capitalizedFieldName = ParserCommonUtils.capitalize(fieldName);

    // Create proper ClassName for the enum type
    final ClassName enumType;
    if (fieldType.isMemberClass()) {
      // For inner enums, we need to include the enclosing class
      Class<?> enclosingClass = fieldType.getEnclosingClass();
      enumType = ClassName.get(enclosingClass.getPackage().getName(),
          enclosingClass.getSimpleName(),
          fieldType.getSimpleName());
    } else {
      enumType = ClassName.get(fieldType);
    }

    // Common enum parsing logic for both primitive and object types
    code.beginControlFlow(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true))
        .addStatement("$T $LStr = $L.getString($S)", String.class, fieldName, objVarName, fieldName)
        .beginControlFlow("if ($LStr != null)", fieldName)
        .beginControlFlow("try")
        .addStatement("config.set$L($T.valueOf($LStr))", capitalizedFieldName, enumType, fieldName)
        .nextControlFlow("catch ($T e)", IllegalArgumentException.class)
        .add("// Invalid enum value, leave as default\n")
        .endControlFlow()
        .endControlFlow()
        .endControlFlow();

    return code.build();
  }
}