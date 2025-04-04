package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
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
  // Map implementations
  private static final ClassName HASH_MAP = ClassName.get("java.util", "HashMap");
  private static final ClassName LINKED_HASH_MAP = ClassName.get("java.util", "LinkedHashMap");
  private static final ClassName LIST = ClassName.get("java.util", "List");
  private static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");

  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getGenericType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (!(type instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType paramType = (ParameterizedType) type;
    if (!(paramType.getRawType() instanceof Class<?>)) {
      return false;
    }
    return Map.class.isAssignableFrom((Class<?>) paramType.getRawType());
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
    return generateParsingCode(type, objVarName, parserPackage, null);
  }

  @Override
  public CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage, String fieldName) {
    CodeBlock.Builder code = CodeBlock.builder();
    CodeBlock accessExpression;
    if (fieldName != null) {
      accessExpression = ParserCommonUtils.createFieldAccessCode(type, objVarName, CodeBlock.of("$S", fieldName));
      code.add(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true, innerCode -> {
        String resultVar = generateParsingCodeInto(innerCode, type, objVarName, parserPackage, accessExpression, 1);
        innerCode.addStatement("// Assign the result: config.set$L($L);", ParserCommonUtils.capitalize(fieldName), resultVar);
      }));
    } else {
      accessExpression = CodeBlock.of("$L", objVarName);
      generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, 1);
    }
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

    // Check for unsupported key/value types BEFORE generating parsing code
    if (ParserCommonUtils.isInterface(keyType) || ParserCommonUtils.containsWildcard(keyType)) {
      code.addStatement("// Skipping map field due to unsupported interface or wildcard in key type: $L", keyType.getTypeName());
      code.addStatement("final $T $L = null", fieldType, mapVar); // Declare null var
      return mapVar;
    }
    if (ParserCommonUtils.isInterface(valueType) || ParserCommonUtils.containsWildcard(valueType)) {
      code.addStatement("// Skipping map field due to unsupported interface or wildcard in value type: $L", valueType.getTypeName());
      code.addStatement("final $T $L = null", fieldType, mapVar); // Declare null var
      return mapVar;
    }

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
    boolean handled = false;

    if (keyType.equals(Integer.class)) {
      keyExpression = CodeBlock.of("Integer.parseInt($L)", keyVar);
      handled = true;
    } else if (keyType instanceof Class<?> && ((Class<?>) keyType).isEnum()) {
      // Enum keys were handled earlier by creating an intermediate variable.
      // This part of addPutStatement should ideally not be reached for enums now.
      // However, keeping the check for robustness or if called directly.
      keyExpression = CodeBlock.of("$T.valueOf($L)", keyType, keyVar);
      handled = true;
    } else if (keyType instanceof Class<?>) {
      // Check for complex key type with a fromStringValue method (like TestComplexKeyType)
      Class<?> keyClass = (Class<?>) keyType;
      try {
        // Look for a static method named "fromStringValue" that accepts a String
        java.lang.reflect.Method fromStringMethod = keyClass.getMethod("fromStringValue", String.class);
        if (java.lang.reflect.Modifier.isStatic(fromStringMethod.getModifiers()) &&
            keyClass.isAssignableFrom(fromStringMethod.getReturnType())) {
          keyExpression = CodeBlock.of("$T.fromStringValue($L)", keyType, keyVar);
          handled = true;
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