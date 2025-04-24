package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;

/**
 * Parser for primitive array fields (int[], byte[], etc.).
 */
public class PrimitiveArrayFieldParser implements TypeParser {
  private static final ClassName LIST = ClassName.get("java.util", "List");

  // Map primitive component types to the specific getter method on JSONArrayHandle
  private static final Map<Class<?>, String> PRIMITIVE_COMPONENT_TO_GETTER = new HashMap<>();

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
    return clazz.isArray() && PRIMITIVE_COMPONENT_TO_GETTER.containsKey(clazz.getComponentType());
    // Only handle primitives with direct getters for simplicity now.
    // return clazz.isArray() && clazz.getComponentType().isPrimitive();
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

    Class<?> arrayType = (Class<?>) type;
    Class<?> componentType = arrayType.getComponentType();
    TypeName declarationTypeName = TypeName.get(fieldType);
    String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "Array");

    // Declare the final result array variable (initialized to null)
    code.addStatement("$T $L = null", declarationTypeName, resultVarName);

    // Add check for field existence and non-null array
    code.beginControlFlow("if ($L.has($L) && !$L.isNull($L))",
        objVarName, accessExpression, objVarName, accessExpression);

    // Handle String, Integer, Double using getXyzArray and List conversion
    if (componentType.equals(String.class) || componentType.equals(Integer.class) || componentType.equals(Double.class)
        || componentType.equals(int.class) || componentType.equals(double.class)) {
      // ... (logic for String/Int/Double using getXyzArray and List conversion - should be correct from before) ...
      String getterMethodName;
      TypeName listItemTypeName;
      if (componentType.equals(String.class)) {
        getterMethodName = "getStringArray";
        listItemTypeName = ClassName.get(String.class);
      } else if (componentType.equals(int.class) || componentType.equals(Integer.class)) {
        getterMethodName = "getIntegerArray";
        listItemTypeName = ClassName.get(Integer.class);
      } else { // double or Double
        getterMethodName = "getNumberArray";
        listItemTypeName = ClassName.get(Double.class);
      }

      String listVarName = ParserCommonUtils.getVariableNameForLevel(level, "List");
      ClassName listClassName = ClassName.get(java.util.List.class);
      TypeName listOfItemType = ParameterizedTypeName.get(listClassName, listItemTypeName.box());

      code.addStatement("final $T $L = $L.$L($L)",
          listOfItemType, listVarName, objVarName, getterMethodName, accessExpression);

      code.beginControlFlow("if ($L != null)", listVarName);
      if (componentType.isPrimitive()) {
        if (componentType.equals(int.class)) {
          code.addStatement("$L = $L.stream().mapToInt(i -> i != null ? i.intValue() : 0).toArray()",
              resultVarName, listVarName);
        } else if (componentType.equals(double.class)) {
          code.addStatement("$L = $L.stream().mapToDouble(d -> d != null ? d.doubleValue() : 0.0).toArray()",
              resultVarName, listVarName);
        }
      } else {
        code.addStatement("$L = $L.toArray(($T) $T.newInstance($T.class, 0))",
            resultVarName, listVarName, declarationTypeName, java.lang.reflect.Array.class, componentType);
      }
      code.endControlFlow(); // End if (listVar != null)

    } else if (componentType.equals(boolean.class) || componentType.equals(Boolean.class)) {
      // GENERATE code using forEachString and manual conversion
      String jsonArrayVar = ParserCommonUtils.getVariableNameForLevel(level, "JsonArrayHandle");
      String stringListVar = ParserCommonUtils.getVariableNameForLevel(level, "StringList");
      ClassName jsonArrayHandleName = ClassName.get("nl.aerius.json", "JSONArrayHandle");
      ClassName arrayListName = ClassName.get(java.util.ArrayList.class);
      TypeName stringListName = ParameterizedTypeName.get(ClassName.get(java.util.List.class), ClassName.get(String.class));

      code.addStatement("final $T $L = $L.getArray($L)", jsonArrayHandleName, jsonArrayVar, objVarName, accessExpression);
      code.beginControlFlow("if ($L != null)", jsonArrayVar);
      code.addStatement("final $T $L = new $T<>()", stringListName, stringListVar, arrayListName);
      code.addStatement("$L.forEachString(s -> $L.add(s))", jsonArrayVar, stringListVar);
      code.addStatement("$L = new $T[$L.size()]", resultVarName, componentType, stringListVar);
      code.beginControlFlow("for (int i = 0; i < $L.size(); i++)", stringListVar);
      code.addStatement("String str = $L.get(i)", stringListVar);
      if (componentType.isPrimitive()) {
        // Default null/invalid strings to false
        code.addStatement("$L[i] = $S.equalsIgnoreCase(str)", resultVarName, "true");
      } else {
        // Handle wrapper Boolean[] - map null strings to null, invalid strings to null
        code.beginControlFlow("if (str == null)")
            .addStatement("$L[i] = null", resultVarName)
            .nextControlFlow("else if ($S.equalsIgnoreCase(str))", "true")
            .addStatement("$L[i] = $T.TRUE", resultVarName, Boolean.class)
            .nextControlFlow("else if ($S.equalsIgnoreCase(str))", "false")
            .addStatement("$L[i] = $T.FALSE", resultVarName, Boolean.class)
            .nextControlFlow("else")
            .addStatement("$L[i] = null", resultVarName) // Or Boolean.FALSE? Defaulting to null.
            .endControlFlow();
      }
      code.endControlFlow(); // End for loop
      code.endControlFlow(); // End if (jsonArrayVar != null)

    } else {
      // Should not happen due to canHandle check
      code.addStatement("// Unsupported array type: $T", componentType);
    }
    code.endControlFlow(); // End if (baseObj.has(...) && !baseObj.isNull(...))

    return resultVarName;
  }
}