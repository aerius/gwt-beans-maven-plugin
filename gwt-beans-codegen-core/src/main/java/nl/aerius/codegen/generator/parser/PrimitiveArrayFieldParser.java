package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;

/**
 * Parser for primitive and wrapper array fields (String[], int[], Integer[], double[], Double[]).
 */
public class PrimitiveArrayFieldParser implements TypeParser {

  // Removed unused static maps

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
    if (!clazz.isArray()) {
      return false;
    }
    Class<?> componentType = clazz.getComponentType();
    // Check for supported component types (primitive or wrapper or String)
    // EXCLUDING boolean/Boolean
    return componentType.equals(String.class) ||
        componentType.equals(int.class) || componentType.equals(Integer.class) ||
        componentType.equals(double.class) || componentType.equals(Double.class);
  }

  // --- Deprecated Methods --- 
  // These are kept for potential backward compatibility but are not used by the core logic.
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
    // Implementation omitted as it's deprecated and complex to map to the new style.
    // Returning an empty block or throwing an exception might be appropriate.
    return CodeBlock.builder().addStatement("// Deprecated generateParsingCode called for $L", fieldName).build();
  }
  // --- End Deprecated Methods --- 


  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    // Delegate to the version with fieldType, using type as fieldType as a default
    return generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, type);
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level, Type fieldType) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("PrimitiveArrayFieldParser cannot handle type: " + type.getTypeName());
    }

    // --- Extract simple field name from accessExpression --- 
    String accessExpressionString = accessExpression.toString();
    String fieldNameString = "";
    int lastQuote = accessExpressionString.lastIndexOf('"');
    int secondLastQuote = accessExpressionString.lastIndexOf('"', lastQuote - 1);
    if (lastQuote > secondLastQuote && secondLastQuote != -1) {
        fieldNameString = accessExpressionString.substring(secondLastQuote + 1, lastQuote);
      } else {
       String simpleAccess = accessExpression.toString().replace("\"", "");
       if (simpleAccess.matches("^[a-zA-Z0-9_]+$")) {
           fieldNameString = simpleAccess;
       } else {
         // If extraction fails, it indicates an unexpected accessExpression format.
         throw new IllegalArgumentException("Could not extract simple field name from access expression: " + accessExpressionString);
       }
    }
    CodeBlock fieldNameCodeBlock = CodeBlock.of("$S", fieldNameString);
    // --- End field name extraction ---

    Class<?> arrayType = (Class<?>) type;
    Class<?> componentType = arrayType.getComponentType();
    TypeName declarationTypeName = TypeName.get(fieldType);
    String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "Array");

    code.addStatement("$T $L = null", declarationTypeName, resultVarName);

    String forEachMethodName;
    Class<?> wrapperType;
    if (componentType.equals(String.class)) {
        forEachMethodName = "forEachString";
        wrapperType = String.class;
    } else if (componentType.equals(int.class) || componentType.equals(Integer.class)) {
        forEachMethodName = "forEachInteger";
        wrapperType = Integer.class;
    } else if (componentType.equals(double.class) || componentType.equals(Double.class)) {
        forEachMethodName = "forEachNumber";
        wrapperType = Double.class;
    } else {
      // Should not happen due to canHandle check
        code.endControlFlow();
        return resultVarName; 
    }

    String jsonArrayVar = ParserCommonUtils.getVariableNameForLevel(level, "JsonArray");
    String listVarName = ParserCommonUtils.getVariableNameForLevel(level, "TempList");
    ClassName jsonArrayHandleName = ClassName.get("nl.aerius.json", "JSONArrayHandle");
    ClassName arrayListName = ClassName.get(java.util.ArrayList.class);
    TypeName wrapperListName = ParameterizedTypeName.get(ClassName.get(java.util.List.class), ClassName.get(wrapperType));

    code.addStatement("final $T $L = $L.getArray($L)", jsonArrayHandleName, jsonArrayVar, objVarName, fieldNameCodeBlock);

    code.beginControlFlow("if ($L != null)", jsonArrayVar);
    code.addStatement("final $T $L = new $T<>()", wrapperListName, listVarName, arrayListName);
    code.addStatement("$L.$L($L::add)", jsonArrayVar, forEachMethodName, listVarName);

    if (componentType.isPrimitive()) {
        if (componentType.equals(int.class)) {
            code.addStatement("$L = $L.stream().mapToInt(i -> i != null ? i.intValue() : 0).toArray()",
                resultVarName, listVarName);
        } else if (componentType.equals(double.class)) {
            code.addStatement("$L = $L.stream().mapToDouble(d -> d != null ? d.doubleValue() : 0.0).toArray()",
                resultVarName, listVarName);
          }
    } else {
      code.addStatement("$L = $L.toArray(new $T[0])",
          resultVarName, listVarName, componentType);
    }
    code.endControlFlow(); // End if (jsonArrayVar != null)

    return resultVarName;
  }
}