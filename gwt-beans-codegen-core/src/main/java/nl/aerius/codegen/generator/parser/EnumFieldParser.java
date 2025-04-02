package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

/**
 * Parser for enum fields.
 */
public class EnumFieldParser implements TypeParser {
  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof Class<?>)) {
      return false;
    }
    return ((Class<?>) type).isEnum();
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
      throw new IllegalArgumentException("EnumFieldParser only handles Class types");
    }
    Class<?> enumClass = (Class<?>) type;
    final CodeBlock.Builder code = CodeBlock.builder();

    // Create proper ClassName for the enum type
    final ClassName enumType;
    if (enumClass.isMemberClass()) {
      // For inner enums, we need to include the enclosing class
      Class<?> enclosingClass = enumClass.getEnclosingClass();
      enumType = ClassName.get(enclosingClass.getPackage().getName(),
          enclosingClass.getSimpleName(),
          enumClass.getSimpleName());
    } else {
      enumType = ClassName.get(enumClass);
    }

    // Common enum parsing logic for both primitive and object types
    code.beginControlFlow("if ($L.has($S) && !$L.isNull($S))", objVarName, fieldName, objVarName, fieldName)
        .addStatement("final $T $LStr = $L.getString($S)", String.class, fieldName, objVarName, fieldName)
        .beginControlFlow("if ($LStr != null)", fieldName)
        .beginControlFlow("try")
        .addStatement("config.set$L($T.valueOf($LStr))", ParserCommonUtils.capitalize(fieldName), enumType, fieldName)
        .nextControlFlow("catch ($T e)", IllegalArgumentException.class)
        .add("// Invalid enum value, leave as default\n")
        .endControlFlow()
        .endControlFlow()
        .endControlFlow();

    return code.build();
  }
}