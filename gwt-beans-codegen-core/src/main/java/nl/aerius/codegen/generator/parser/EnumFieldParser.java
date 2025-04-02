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
    return canHandle(field.getGenericType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof Class<?>)) {
      return false;
    }
    return ((Class<?>) type).isEnum();
  }

  @Override
  @Deprecated
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage) {
    return generateParsingCode(field.getGenericType(), objVarName, parserPackage, field.getName());
  }

  @Override
  @Deprecated
  public CodeBlock generateParsingCode(Field field, String objVarName, String parserPackage, String fieldName) {
    return generateParsingCode(field.getGenericType(), objVarName, parserPackage, fieldName);
  }

  @Override
  @Deprecated
  public CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage) {
    return generateParsingCode(type, objVarName, parserPackage, "value");
  }

  @Override
  @Deprecated
  public CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage, String fieldName) {
    if (!(type instanceof Class<?>)) {
      throw new IllegalArgumentException("EnumFieldParser only handles Class types (Deprecated Method)");
    }
    CodeBlock.Builder code = CodeBlock.builder();
    CodeBlock accessExpression = ParserCommonUtils.createFieldAccessCode(type, objVarName, CodeBlock.of("$S", fieldName));

    code.add(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true, innerCode -> {
      String resultVar = generateParsingCodeInto(innerCode, type, objVarName, parserPackage, accessExpression, 1);
      innerCode.addStatement("// Assign result: config.set$L($L);", ParserCommonUtils.capitalize(fieldName), resultVar);
    }));
    return code.build();
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("EnumFieldParser cannot handle type: " + type.getTypeName());
    }
    Class<?> enumClass = (Class<?>) type;
    String resultVarName = "level" + level + "Value";
    String tempStringVar = "level" + level + "Str";

    final ClassName enumType = ClassName.get(enumClass);

    code.addStatement("final $T $L = $L", String.class, tempStringVar, accessExpression);

    code.addStatement("final $T $L = null", enumType, resultVarName);

    code.beginControlFlow("if ($L != null)", tempStringVar);
    code.beginControlFlow("try");
    code.addStatement("$L = $T.valueOf($L)", resultVarName, enumType, tempStringVar);
    code.nextControlFlow("catch ($T e)", IllegalArgumentException.class);
    code.addStatement("// Invalid enum value $S, leaving $L as null", "[" + tempStringVar + "]", resultVarName);
    code.endControlFlow();
    code.endControlFlow();

    return resultVarName;
  }
}