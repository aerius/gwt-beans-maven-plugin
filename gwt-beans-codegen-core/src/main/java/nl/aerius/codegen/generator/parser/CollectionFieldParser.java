package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;
import nl.aerius.codegen.util.ClassFinder;
import nl.aerius.codegen.util.Logger;

/**
 * Parser for Collection fields (List, Set, etc.) and Object arrays.
 */
public class CollectionFieldParser implements TypeParser {
  // Collection type implementations
  private static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");
  private static final ClassName HASH_SET = ClassName.get("java.util", "HashSet");
  private final ClassFinder classFinder;
  private final Logger logger;

  public CollectionFieldParser(final ClassFinder classFinder, final Logger logger) {
    this.classFinder = classFinder;
    this.logger = logger;
  }

  @Override
  public boolean canHandle(final Type type) {
    if (type instanceof Class<?>) {
      final Class<?> clazz = (Class<?>) type;
      if (!clazz.isArray()) {
        return false; // Only interested in arrays here
      }
      final Class<?> componentType = clazz.getComponentType();
      // Exclude primitive arrays AND common wrapper/String arrays
      return !componentType.isPrimitive() &&
          !componentType.equals(String.class) &&
          !componentType.equals(Integer.class) &&
          !componentType.equals(Long.class) && // Consider Long as well
          !componentType.equals(Double.class) &&
          !componentType.equals(Float.class) && // Consider Float
          !componentType.equals(Boolean.class) &&
          !componentType.equals(Byte.class) && // Consider Byte
          !componentType.equals(Short.class) && // Consider Short
          !componentType.equals(Character.class); // Consider Character
      // This leaves arrays of custom objects (e.g., MyType[])
    }
    if (type instanceof ParameterizedType) {
      final ParameterizedType paramType = (ParameterizedType) type;
      if (!(paramType.getRawType() instanceof Class<?>)) {
        return false;
      }
      // Handle Collection<...> types
      return Collection.class.isAssignableFrom((Class<?>) paramType.getRawType());
    }
    if (type instanceof java.lang.reflect.GenericArrayType) {
      // Handle generic arrays like T[]
      return true;
    }
    return false;
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
      throw new IllegalArgumentException("CollectionFieldParser cannot handle type: " + type.getTypeName());
    }

    // Determine if it's a Collection<E> or an Object E[]
    if (type instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType())) {
      return generateCollectionParsingCodeInto(code, (ParameterizedType) type, parserPackage, accessExpression, level, fieldType);
    } else if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
      return generateObjectArrayParsingCodeInto(code, (Class<?>) type, parserPackage, accessExpression, level, fieldType);
    } else if (type instanceof java.lang.reflect.GenericArrayType) {
      return generateGenericArrayParsingCodeInto(code, (java.lang.reflect.GenericArrayType) type, parserPackage, accessExpression, level,
          fieldType);
    } else {
      throw new IllegalArgumentException("Unhandled type in CollectionFieldParser: " + type.getTypeName());
    }
  }

  // Handles Collection<E>
  private String generateCollectionParsingCodeInto(final CodeBlock.Builder code, final ParameterizedType collectionRuntimeType,
      final String parserPackage,
      final CodeBlock accessExpression, final int level, final Type fieldType) {
    final Type elementType = collectionRuntimeType.getActualTypeArguments()[0];
    // Use fieldType to determine the variable declaration type
    final Type variableDeclarationType = fieldType;
    String resultVarName; // Determine based on variableDeclarationType
    ClassName collectionImpl; // Determine based on variableDeclarationType

    // Determine implementation and set resultVarName prefix
    if (variableDeclarationType instanceof Class<?> && Set.class.isAssignableFrom((Class<?>) variableDeclarationType)) {
      resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "Set");
      collectionImpl = HASH_SET;
    } else if (variableDeclarationType instanceof ParameterizedType
        && Set.class.isAssignableFrom((Class<?>) ((ParameterizedType) variableDeclarationType).getRawType())) {
      resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "Set");
      collectionImpl = HASH_SET;
    } else { // Default to List/ArrayList
      resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "List");
      collectionImpl = ARRAY_LIST;
    }

    // Skip if element type is a wildcard or TypeVariable for now
    if (elementType instanceof java.lang.reflect.WildcardType || elementType instanceof java.lang.reflect.TypeVariable) {
      code.addStatement("// Skipping collection with complex generic type argument: $L", collectionRuntimeType.getTypeName());
      code.addStatement("$T $L = null", variableDeclarationType, resultVarName); // Declare null var with interface type
      return resultVarName;
    }

    final String arrayVar = ParserCommonUtils.getVariableNameForLevel(level, "Array");
    final String itemVar = ParserCommonUtils.getVariableNameForLevel(level, "Item");

    // 1. Get JSON Array
    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONArrayHandle(), arrayVar, accessExpression);
    // 2. Declare using the exact fieldType, instantiate using IMPL
    code.addStatement("final $T $L = new $T<>()", fieldType, resultVarName, collectionImpl);

    final String specificForEach = getSpecificForEachMethod(elementType);

    if (specificForEach != null) {
      // Handle simple types with specific forEach methods
      code.addStatement("$L.$L($L::add)", arrayVar, specificForEach, resultVarName);
    } else if (elementType instanceof Class<?> && ((Class<?>) elementType).isEnum()) {
      final Class<?> enumElementType = (Class<?>) elementType;
      final Method jsonCreatorMethod = ParserCommonUtils.findJsonCreatorMethod(enumElementType, classFinder, logger); // Use common util
      final String strVar = itemVar; // Reuse itemVar name for the string in the lambda
      final String enumValueVar = ParserCommonUtils.getVariableNameForLevel(level + 1, "Value");

      code.add("$L.forEachString($L -> {\n", arrayVar, strVar)
          .indent()
          .addStatement("$T $L = null", enumElementType, enumValueVar)
          .beginControlFlow("if ($L != null)", strVar);

      if (jsonCreatorMethod != null) {
        // Use @JsonCreator if found
        code.addStatement("$L = $T.$L($L)", enumValueVar, enumElementType, jsonCreatorMethod.getName(), strVar);
      } else {
        // Fallback to valueOf()
        code.beginControlFlow("try")
            .addStatement("$L = $T.valueOf($L)", enumValueVar, enumElementType, strVar)
            .nextControlFlow("catch ($T e)", IllegalArgumentException.class)
            .add("// Invalid enum value, leave as default\n")
            .endControlFlow();
      }

      code.endControlFlow() // End if (strVar != null)
          .addStatement("$L.add($L)", resultVarName, enumValueVar)
          .unindent()
          .addStatement("})"); // Add closing parenthesis for lambda
    } else {
      // Handle complex types (Objects, other Collections/Maps) using generic forEach and dispatch
      code.add("$L.forEach($L -> {\n", arrayVar, itemVar)
          .indent();
      final Type elementFieldType = getElementTypeFromCollectionType(fieldType);
      final String elementVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
          code, elementType, null, parserPackage, CodeBlock.of("$L", itemVar), level + 1, elementFieldType);
      code.addStatement("$L.add($L)", resultVarName, elementVarName);
      code.unindent().addStatement("})");
    }
    return resultVarName;
  }

  // Handles Object E[] (e.g., String[], CustomObject[])
  private String generateObjectArrayParsingCodeInto(final CodeBlock.Builder code, final Class<?> arrayRuntimeType, final String parserPackage,
      final CodeBlock accessExpression, final int level, final Type fieldType) {
    final Type componentType = arrayRuntimeType.getComponentType();
    // Use helper for variable names
    final String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "Array");
    final String arrayJsonVar = ParserCommonUtils.getVariableNameForLevel(level, "JsonArray");
    final String itemVar = ParserCommonUtils.getVariableNameForLevel(level, "Item");
    final String indexVar = ParserCommonUtils.getVariableNameForLevel(level, "Index");

    // 1. Get the JSON Array - Use $T for ClassName
    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONArrayHandle(), arrayJsonVar, accessExpression);
    // 2. Create the Java Array instance
    // Use length() method of JSONArrayHandle
    code.addStatement("final $T[] $L = new $T[$L.length()]",
        fieldType, resultVarName, componentType, arrayJsonVar);
    // 3. Loop over the JSON Array using forEachWithIndex
    code.add("$L.forEachWithIndex(($L, $L) -> {\n", arrayJsonVar, itemVar, indexVar)
        .indent();

    // 4. Dispatch parsing for the component type
    final Type componentFieldType = getComponentTypeFromArrayType(fieldType);
    final String elementVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
        code,
        componentType,
        null,
        parserPackage,
        CodeBlock.of("$L", itemVar),
        level + 1,
        componentFieldType);

    // 5. Assign the parsed element to the array index
    code.addStatement("$L[$L] = $L", resultVarName, indexVar, elementVarName);

    // 6. End loop
    code.unindent()
        .addStatement("})");

    return resultVarName;
  }

  // Handles GenericArrayType (e.g., T[]) - Experimental
  private String generateGenericArrayParsingCodeInto(final CodeBlock.Builder code, final java.lang.reflect.GenericArrayType arrayRuntimeType,
      final String parserPackage, final CodeBlock accessExpression, final int level, final Type fieldType) {
    final Type componentType = arrayRuntimeType.getGenericComponentType();
    // Use helper for variable names
    final String resultVarName = ParserCommonUtils.getVariableNameForLevel(level, "Array");
    final String arrayJsonVar = ParserCommonUtils.getVariableNameForLevel(level, "JsonArray");
    final String itemVar = ParserCommonUtils.getVariableNameForLevel(level, "Item");
    final String tempListVar = ParserCommonUtils.getVariableNameForLevel(level, "TempList");

    // Cannot directly create generic array T[]. Create List first, then convert.
    // 1. Get the JSON Array - Use $T for ClassName
    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONArrayHandle(), arrayJsonVar, accessExpression);
    // 2. Create intermediate List
    code.addStatement("final $T<$T> $L = new $T<>()", java.util.List.class, componentType, tempListVar, java.util.ArrayList.class);

    // 3. Loop over JSON Array, parse elements into List (use generic forEach)
    code.add("$L.forEach($L -> {\n", arrayJsonVar, itemVar)
        .indent();
    final Type componentFieldType = getComponentTypeFromArrayType(fieldType);
    final String elementVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
        code,
        componentType,
        null,
        parserPackage,
        CodeBlock.of("$L", itemVar),
        level + 1,
        componentFieldType);
    code.addStatement("$L.add($L)", tempListVar, elementVarName);
    code.unindent()
        .addStatement("})");

    // 4. Convert List to Array (Commented out / placeholder)
    code.addStatement("// TODO: Convert $L to array of $L", tempListVar, componentType.getTypeName());
    code.addStatement("final $T[] $L = null; // Cannot directly create generic array", Object.class, resultVarName);

    return resultVarName;
  }

  // Helper method to get the specific forEach method name
  private String getSpecificForEachMethod(final Type elementType) {
    if (elementType.equals(String.class)) {
      return "forEachString";
    } else if (elementType.equals(Integer.class)) {
      return "forEachInteger";
    } else if (elementType.equals(Double.class)) {
      return "forEachNumber"; // JSONArrayHandle uses forEachNumber for Double
    } // No forEachBoolean exists in the test JSONArrayHandle
    return null; // No specific method for this type
  }

  private Type getElementTypeFromCollectionType(final Type collectionType) {
    if (collectionType instanceof ParameterizedType) {
      final ParameterizedType paramType = (ParameterizedType) collectionType;
      if (Collection.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
        return paramType.getActualTypeArguments()[0];
      }
    }
    throw new IllegalArgumentException("Cannot determine element type from collection type: " + collectionType.getTypeName());
  }

  private Type getComponentTypeFromArrayType(final Type arrayType) {
    if (arrayType instanceof java.lang.reflect.GenericArrayType) {
      return ((java.lang.reflect.GenericArrayType) arrayType).getGenericComponentType();
    } else if (arrayType instanceof Class<?> && ((Class<?>) arrayType).isArray()) { // Handle non-generic arrays
      return ((Class<?>) arrayType).getComponentType();
    }
    throw new IllegalArgumentException("Cannot determine component type from array type: " + arrayType.getTypeName());
  }
}