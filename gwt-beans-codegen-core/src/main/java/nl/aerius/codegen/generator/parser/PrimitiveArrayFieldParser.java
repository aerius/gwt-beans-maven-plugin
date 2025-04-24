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
  private static final ClassName LIST = ClassName.get("java.util", "List");

  // Map primitive component types to the specific getter method on JSONArrayHandle
  private static final java.util.Map<Class<?>, String> PRIMITIVE_COMPONENT_TO_GETTER = new java.util.HashMap<>();

  static {
    // Assumes JSONArrayHandle has methods like getStringArray(), getIntegerArray() etc.
    // Adjust method names if they differ in nl.aerius.wui.service.json.JSONArrayHandle
    PRIMITIVE_COMPONENT_TO_GETTER.put(int.class, "getIntegerArray");
    PRIMITIVE_COMPONENT_TO_GETTER.put(long.class, "getLongArray"); // Assuming getLongArray exists
    PRIMITIVE_COMPONENT_TO_GETTER.put(double.class, "getNumberArray");
    PRIMITIVE_COMPONENT_TO_GETTER.put(boolean.class, "getBooleanArray");
    // Note: byte[], short[], float[], char[] might not have direct getters
    // and may require iterating and casting, similar to non-primitive arrays.
    // If they DO have direct getters (e.g., getByteArray), add them here.
    // For now, we only handle int[], long[], double[], boolean[].
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

  // --- New Recursive Parsing Method ---
  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    return generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, type);
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level, Type fieldType) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("PrimitiveArrayFieldParser cannot handle type: " + type.getTypeName());
    }

    // --- Extract simple field name from accessExpression (Still needed) --- 
    String accessExpressionString = accessExpression.toString();
    String fieldNameString = "";
    int lastQuote = accessExpressionString.lastIndexOf('"');
    int secondLastQuote = accessExpressionString.lastIndexOf('"', lastQuote - 1);
    if (lastQuote > secondLastQuote && secondLastQuote != -1) {
        fieldNameString = accessExpressionString.substring(secondLastQuote + 1, lastQuote);
    } else {
       // Fallback: Assume accessExpression might just be the string literal if extraction fails
       // This handles cases where the accessExpression was already simplified
       String simpleAccess = accessExpression.toString().replace("\"", "");
       if (simpleAccess.matches("^[a-zA-Z0-9_]+$")) { // Basic check for valid field name
           fieldNameString = simpleAccess;
       } else {
           code.addStatement("// ERROR: Could not extract field name from access expression: $L", accessExpression);
           fieldNameString = "extractedFieldNameError";
       }
    }
    CodeBlock fieldNameCodeBlock = CodeBlock.of("$S", fieldNameString);
    // --- End field name extraction ---

    Class<?> arrayType = (Class<?>) type;
    Class<?> componentType = arrayType.getComponentType();
    TypeName declarationTypeName = TypeName.get(fieldType); // The final array type (e.g., String[], int[])
    String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "Array");

    // Declare the final result array variable (initialized to null)
    code.addStatement("$T $L = null", declarationTypeName, resultVarName);

    // Add check for field existence and non-null array
    code.beginControlFlow("if ($L.has($L) && !$L.isNull($L))",
        objVarName, fieldNameCodeBlock, objVarName, fieldNameCodeBlock);

    // Determine the JSONArrayHandle forEach method and temporary List type
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
        // Should not happen due to canHandle
        code.addStatement("// Should not happen: Unsupported array type in generate: $T", componentType);
        code.endControlFlow();
        return resultVarName; 
    }

    String jsonArrayVar = ParserCommonUtils.getVariableNameForLevel(level, "JsonArray");
    String listVarName = ParserCommonUtils.getVariableNameForLevel(level, "TempList");
    ClassName jsonArrayHandleName = ClassName.get("nl.aerius.json", "JSONArrayHandle");
    ClassName arrayListName = ClassName.get(java.util.ArrayList.class);
    TypeName wrapperListName = ParameterizedTypeName.get(ClassName.get(java.util.List.class), ClassName.get(wrapperType));

    // 1. Get JSONArrayHandle
    code.addStatement("final $T $L = $L.getArray($L)", jsonArrayHandleName, jsonArrayVar, objVarName, fieldNameCodeBlock);

    // 2. Create temporary List and populate using forEachXyz
    code.beginControlFlow("if ($L != null)", jsonArrayVar);
    code.addStatement("final $T $L = new $T<>()", wrapperListName, listVarName, arrayListName);
    code.addStatement("$L.$L($L::add)", jsonArrayVar, forEachMethodName, listVarName);

    // 3. Convert temporary List to final target array
    if (componentType.isPrimitive()) {
        // Primitive array conversion (int[], double[])
        if (componentType.equals(int.class)) {
            code.addStatement("$L = $L.stream().mapToInt(i -> i != null ? i.intValue() : 0).toArray()",
                resultVarName, listVarName);
        } else if (componentType.equals(double.class)) {
            code.addStatement("$L = $L.stream().mapToDouble(d -> d != null ? d.doubleValue() : 0.0).toArray()",
                resultVarName, listVarName);
        }
        // Add boolean[] here if/when forEachBoolean is reliably available
    } else {
        // Wrapper/String array conversion (String[], Integer[], Double[])
        code.addStatement("$L = $L.toArray(($T) $T.newInstance($T.class, 0))",
            resultVarName, listVarName, declarationTypeName, java.lang.reflect.Array.class, componentType);
    }
    code.endControlFlow(); // End if (jsonArrayVar != null)

    code.endControlFlow(); // End if (baseObj.has(...) && !baseObj.isNull(...))

    return resultVarName;
  }
}