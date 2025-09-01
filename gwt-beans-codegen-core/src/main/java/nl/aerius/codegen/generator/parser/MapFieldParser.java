package nl.aerius.codegen.generator.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;
import nl.aerius.codegen.util.Logger;

/**
 * Parser for Map fields.
 */
public class MapFieldParser implements TypeParser {

  private final Logger logger;

  public MapFieldParser(final Logger logger) {
    this.logger = logger;
  }

  @Override
  public boolean canHandle(final Type type) {
    if (!(type instanceof ParameterizedType)) {
      return false;
    }
    final ParameterizedType paramType = (ParameterizedType) type;
    if (!(paramType.getRawType() instanceof Class<?>)) {
      return false;
    }
    if (!Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
      return false;
    }

    // Check if the Map contains interfaces or wildcards in its type arguments
    final Type keyType = paramType.getActualTypeArguments()[0];
    final Type valueType = paramType.getActualTypeArguments()[1];

    // If either key or value type contains interfaces or wildcards, we can't handle it
    if (ParserCommonUtils.isInterface(keyType) || ParserCommonUtils.containsWildcard(keyType) ||
        ParserCommonUtils.containsInterface(valueType) || ParserCommonUtils.containsWildcard(valueType)) {
      return false;
    }

    return true;
  }

  @Override
  public String generateParsingCodeInto(final CodeBlock.Builder code, final Type type, final String objVarName, final String parserPackage,
      final CodeBlock accessExpression, final int level) {
    return generateParsingCodeInto(code, type, objVarName, parserPackage, accessExpression, level, type);
  }

  @Override
  public String generateParsingCodeInto(final CodeBlock.Builder code, final Type type, final String objVarName, final String parserPackage,
      final CodeBlock accessExpression,
      final int level, final Type fieldType) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("MapFieldParser cannot handle type: " + type.getTypeName());
    }
    final ParameterizedType mapRuntimeType = (ParameterizedType) type;
    final Type keyType = mapRuntimeType.getActualTypeArguments()[0];
    final Type valueType = mapRuntimeType.getActualTypeArguments()[1];

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

    final String mapVar = ParserCommonUtils.getVariableNameForLevel(level, "Map");
    final String objVar = ParserCommonUtils.getVariableNameForLevel(level, "Obj");
    final String keyVar = ParserCommonUtils.getVariableNameForLevel(level, "Key");

    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONObjectHandle(), objVar, accessExpression);

    code.addStatement("final $T $L = new $T<>()", fieldType, mapVar, mapImpl);

    code.add("$L.keySet().forEach($L -> {\n", objVar, keyVar)
        .indent();

    final CodeBlock valueAccessExpression = ParserCommonUtils.createFieldAccessCode(
        valueType,
        objVar,
        CodeBlock.of("$L", keyVar));

    final Type valueFieldType = getValueTypeFromMapType(fieldType);
    final String valueVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
        code,
        valueType,
        objVar,
        parserPackage,
        valueAccessExpression,
        level + 1,
        valueFieldType);

    // Introduce intermediate key variable specifically for Enum keys
    if (keyType instanceof Class<?> && ((Class<?>) keyType).isEnum()) {
      final String enumKeyVar = ParserCommonUtils.getVariableNameForLevel(level, "EnumKey");
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

  private void addPutStatement(final CodeBlock.Builder code, final String mapVar, final Type keyType, final String keyVar, final String valueVar) {
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
      final Class<?> keyClass = (Class<?>) keyType;
      try {
        // Look for a static method named "fromStringValue" that accepts a String
        final java.lang.reflect.Method fromStringMethod = keyClass.getMethod("fromStringValue", String.class);
        if (java.lang.reflect.Modifier.isStatic(fromStringMethod.getModifiers()) &&
            keyClass.isAssignableFrom(fromStringMethod.getReturnType())) {
          keyExpression = CodeBlock.of("$T.fromStringValue($L)", keyType, keyVar);
        }
      } catch (final NoSuchMethodException e) {
        // Method not found, continue to default
      } catch (final SecurityException e) {
        // Cannot access method, log or handle
        logger.warn("Warning: SecurityException while checking for fromStringValue method on " + keyClass.getName());
      }
    }

    // If key type wasn't specifically handled, it defaults to using the keyVar as a String.
    // This is correct for String keys, but will cause compile errors for unsupported types.
    code.addStatement("$L.put($L, $L)", mapVar, keyExpression, valueVar);
  }

  private Type getValueTypeFromMapType(final Type mapFieldType) {
    if (mapFieldType instanceof ParameterizedType) {
      final ParameterizedType pt = (ParameterizedType) mapFieldType;
      if (Map.class.isAssignableFrom((Class<?>) pt.getRawType())) {
        final Type[] typeArgs = pt.getActualTypeArguments();
        if (typeArgs.length == 2) {
          return typeArgs[1];
        }
      }
    }
    logger.warn("Warning: Could not extract value type from map type: " + mapFieldType.getTypeName());
    return Object.class;
  }
}