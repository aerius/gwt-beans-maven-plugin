package nl.aerius.codegen.generator.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for Map fields.
 */
public class MapFieldParser implements TypeParser {

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType paramType = (ParameterizedType) type;
    if (!(paramType.getRawType() instanceof Class<?>)) {
      return false;
    }
    if (!Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
      return false;
    }

    // Check if the Map contains interfaces or wildcards in its type arguments
    Type keyType = paramType.getActualTypeArguments()[0];
    Type valueType = paramType.getActualTypeArguments()[1];

    // If either key or value type contains interfaces or wildcards, we can't handle it
    if (ParserCommonUtils.isInterface(keyType) || ParserCommonUtils.containsWildcard(keyType) ||
        ParserCommonUtils.containsInterface(valueType) || ParserCommonUtils.containsWildcard(valueType)) {
      return false;
    }

    return true;
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
      throw new IllegalArgumentException("MapFieldParser cannot handle type: " + type.getTypeName());
    }
    ParameterizedType mapRuntimeType = (ParameterizedType) type;
    Type keyType = mapRuntimeType.getActualTypeArguments()[0];
    Type valueType = mapRuntimeType.getActualTypeArguments()[1];

    // Determine Map implementation based on fieldType
    ClassName mapImpl;
    Type typeForImplCheck = fieldType; // Start with the actual field type

    if (fieldType instanceof ParameterizedType) {
      // If it's ParameterizedType (like HashMap<String, Integer>), 
      // get its raw type (HashMap.class) for the implementation check
      typeForImplCheck = ((ParameterizedType) fieldType).getRawType();
    }

    // Now check if the type (or raw type) is a concrete Map implementation
    if (typeForImplCheck instanceof Class<?> && !((Class<?>) typeForImplCheck).isInterface()
        && Map.class.isAssignableFrom((Class<?>) typeForImplCheck)) {
      mapImpl = ClassName.get((Class<?>) typeForImplCheck); // Use concrete class (e.g., HashMap)
    } else {
      // Default for Map interface or other cases
      mapImpl = ClassName.get(java.util.LinkedHashMap.class);
    }

    String mapVar = ParserCommonUtils.getVariableNameForLevel(level, "Map");
    String objVar = ParserCommonUtils.getVariableNameForLevel(level, "Obj");
    String keyVar = ParserCommonUtils.getVariableNameForLevel(level, "Key");

    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONObjectHandle(), objVar, accessExpression);

    code.addStatement("final $T $L = new $T<>()", fieldType, mapVar, mapImpl);

    code.add("$L.keySet().forEach($L -> {\n", objVar, keyVar)
        .indent();

    CodeBlock valueAccessExpression = ParserCommonUtils.createFieldAccessCode(
        valueType,
        objVar,
        CodeBlock.of("$L", keyVar));

    Type valueFieldType = getValueTypeFromMapType(fieldType);
    String valueVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
        code,
        valueType,
        objVar,
        parserPackage,
        valueAccessExpression,
        level + 1,
        valueFieldType);

    // Introduce intermediate key variable specifically for Enum keys
    if (keyType instanceof Class<?> && ((Class<?>) keyType).isEnum()) {
      String enumKeyVar = ParserCommonUtils.getVariableNameForLevel(level, "EnumKey");
      code.addStatement("final $T $L = $T.valueOf($L)", keyType, enumKeyVar, keyType, keyVar);
      code.addStatement("$L.put($L, $L)", mapVar, enumKeyVar, valueVarName); // Use intermediate variable
    } else {
      // Original logic for other key types (String, Integer, etc.)
      addPutStatement(code, mapVar, keyType, keyVar, valueVarName);
    }

    code.unindent()
        .addStatement("})");

    return mapVar;
  }

  private void addPutStatement(CodeBlock.Builder code, String mapVar, Type keyType, String keyVar, String valueVar) {
    CodeBlock keyExpression = CodeBlock.of("$L", keyVar); // Default to using the string key directly

    if (keyType.equals(Integer.class)) {
      keyExpression = CodeBlock.of("Integer.parseInt($L)", keyVar);
    } else if (keyType instanceof Class<?> && ((Class<?>) keyType).isEnum()) {
      // Enum keys were handled earlier by creating an intermediate variable.
      // This part of addPutStatement should ideally not be reached for enums now.
      // However, keeping the check for robustness or if called directly.
      keyExpression = CodeBlock.of("$T.valueOf($L)", keyType, keyVar);
    } else if (keyType instanceof Class<?>) {
      // Check for complex key type with a fromStringValue method (like TestComplexKeyType)
      Class<?> keyClass = (Class<?>) keyType;
      try {
        // Look for a static method named "fromStringValue" that accepts a String
        java.lang.reflect.Method fromStringMethod = keyClass.getMethod("fromStringValue", String.class);
        if (java.lang.reflect.Modifier.isStatic(fromStringMethod.getModifiers()) &&
            keyClass.isAssignableFrom(fromStringMethod.getReturnType())) {
          keyExpression = CodeBlock.of("$T.fromStringValue($L)", keyType, keyVar);
        }
      } catch (NoSuchMethodException e) {
        // Method not found, continue to default
      } catch (SecurityException e) {
        // Cannot access method, log or handle
        System.err.println("Warning: SecurityException while checking for fromStringValue method on " + keyClass.getName());
      }
    }

    // If key type wasn't specifically handled, it defaults to using the keyVar as a String.
    // This is correct for String keys, but will cause compile errors for unsupported types.
    code.addStatement("$L.put($L, $L)", mapVar, keyExpression, valueVar);
  }

  private Type getValueTypeFromMapType(Type mapFieldType) {
    if (mapFieldType instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) mapFieldType;
      if (Map.class.isAssignableFrom((Class<?>) pt.getRawType())) {
        Type[] typeArgs = pt.getActualTypeArguments();
        if (typeArgs.length == 2) {
          return typeArgs[1];
        }
      }
    }
    System.err.println("Warning: Could not extract value type from map type: " + mapFieldType.getTypeName());
    return Object.class;
  }
}