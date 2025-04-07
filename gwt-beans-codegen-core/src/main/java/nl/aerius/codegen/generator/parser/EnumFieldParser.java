package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

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
    return generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, type);
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level, Type fieldType) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("EnumFieldParser cannot handle type: " + type.getTypeName());
    }
    Class<?> enumType = (Class<?>) type;
    String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "value");
    String strVarName = ParserCommonUtils.getVariableNameForLevel(level, "str");

    // Find a potential @JsonCreator method using the common utility
    Method jsonCreatorMethod = ParserCommonUtils.findJsonCreatorMethod(enumType);

    // Get the string value from JSON
    code.addStatement("final String $L = $L", strVarName, accessExpression);
    // Declare the result variable (nullable)
    code.addStatement("$T $L = null", enumType, resultVarName);

    // Use @JsonCreator if found, otherwise fallback to valueOf()
    code.beginControlFlow("if ($L != null)", strVarName);
    if (jsonCreatorMethod != null) {
      // Assuming the @JsonCreator method handles invalid input gracefully (e.g., returns null)
      code.addStatement("$L = $T.$L($L)", resultVarName, enumType, jsonCreatorMethod.getName(), strVarName);
    } else {
      // Try-catch block for valueOf as it throws IllegalArgumentException
      code.beginControlFlow("try")
          .addStatement("$L = $T.valueOf($L)", resultVarName, enumType, strVarName)
          .nextControlFlow("catch (IllegalArgumentException e)")
          .add("// Invalid enum value, leave as default\n")
          .endControlFlow();
    }
    code.endControlFlow(); // End if (strVarName != null)

    return resultVarName;
  }
}