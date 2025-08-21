package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.util.ClassFinder;
import nl.aerius.codegen.util.Logger;

/**
 * Parser for enum fields.
 */
public class EnumFieldParser implements TypeParser {

  private final ClassFinder classFinder;
  private final Logger logger;

  public EnumFieldParser(final ClassFinder classFinder, final Logger logger) {
    this.classFinder = classFinder;
    this.logger = logger;
  }

  @Override
  public boolean canHandle(final Type type) {
    if (!(type instanceof Class<?>)) {
      return false;
    }
    return ((Class<?>) type).isEnum();
  }

  @Override
  public String generateParsingCodeInto(final CodeBlock.Builder code, final Type type, final String objVarName, final String parserPackage,
      final CodeBlock accessExpression,
      final int level) {
    return generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, type);
  }

  @Override
  public String generateParsingCodeInto(final CodeBlock.Builder code, final Type type, final String objVarName, final String parserPackage,
      final CodeBlock accessExpression,
      final int level, final Type fieldType) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("EnumFieldParser cannot handle type: " + type.getTypeName());
    }
    final Class<?> enumType = (Class<?>) type;
    final String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "value");
    final String strVarName = ParserCommonUtils.getVariableNameForLevel(level, "str");

    // Find a potential @JsonCreator method using the common utility
    final Method jsonCreatorMethod = ParserCommonUtils.findJsonCreatorMethod(enumType, classFinder, logger);

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