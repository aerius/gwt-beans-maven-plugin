package nl.aerius.codegen.generator.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;

import nl.aerius.codegen.generator.ParserWriterUtils;

/**
 * Parser for Collection fields (List, Set, etc.) and Object arrays.
 */
public class CollectionFieldParser implements TypeParser {
  // Collection type implementations
  private static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");
  private static final ClassName HASH_SET = ClassName.get("java.util", "HashSet");

  // Map of element types to their array getter methods
  private static final Map<Type, String> ELEMENT_TYPE_TO_ARRAY_GETTER = new HashMap<>();

  static {
    // Initialize element type to array getter method mapping
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(String.class, "getStringArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Integer.class, "getIntegerArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Double.class, "getNumberArray");
    ELEMENT_TYPE_TO_ARRAY_GETTER.put(Boolean.class, "getBooleanArray");
  }

  @Override
  public boolean canHandle(Field field) {
    return canHandle(field.getGenericType());
  }

  @Override
  public boolean canHandle(Type type) {
    if (type instanceof Class<?>) {
      Class<?> clazz = (Class<?>) type;
      // Handle Object arrays (e.g., String[], CustomObject[])
      return clazz.isArray() && !clazz.getComponentType().isPrimitive();
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) type;
      if (!(paramType.getRawType() instanceof Class<?>)) {
        return false;
      }
      // Handle Collection<...> types
      return Collection.class.isAssignableFrom((Class<?>) paramType.getRawType());
    }
    if (type instanceof java.lang.reflect.GenericArrayType) {
      // Handle generic arrays like T[] where T might be complex later
      return true;
    }
    return false;
  }

  @Override
  public String generateParsingCodeInto(CodeBlock.Builder code, Type type, String objVarName, String parserPackage, CodeBlock accessExpression,
      int level) {
    if (!canHandle(type)) {
      throw new IllegalArgumentException("CollectionFieldParser cannot handle type: " + type.getTypeName());
    }

    // Determine if it's a Collection<E> or an Object E[]
    if (type instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType())) {
      return generateCollectionParsingCodeInto(code, (ParameterizedType) type, objVarName, parserPackage, accessExpression, level);
    } else if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
      return generateObjectArrayParsingCodeInto(code, (Class<?>) type, objVarName, parserPackage, accessExpression, level);
    } else if (type instanceof java.lang.reflect.GenericArrayType) {
      // Handle T[] - Get the component type and treat as object array
      Type componentType = ((java.lang.reflect.GenericArrayType) type).getGenericComponentType();
      // Need the runtime class for array creation, which is tricky with pure generics.
      // For now, attempt to generate assuming componentType resolves to a usable Class<?> at runtime.
      // This might require adjustments based on actual generic usage.
      // Let's try generating code similar to Object array, but using the Type for parsing.
      return generateGenericArrayParsingCodeInto(code, (java.lang.reflect.GenericArrayType) type, objVarName, parserPackage, accessExpression, level);
    } else {
      throw new IllegalArgumentException("Unhandled type in CollectionFieldParser: " + type.getTypeName());
    }
  }

  // Handles Collection<E>
  private String generateCollectionParsingCodeInto(CodeBlock.Builder code, ParameterizedType collectionType, String objVarName, String parserPackage,
      CodeBlock accessExpression, int level) {
    Type elementType = collectionType.getActualTypeArguments()[0];
    Type rawCollectionType = collectionType.getRawType();
    String resultVarName = "level" + level + "List"; // Default name

    // Skip if element type is a wildcard or TypeVariable for now
    if (elementType instanceof java.lang.reflect.WildcardType || elementType instanceof java.lang.reflect.TypeVariable) {
      code.addStatement("// Skipping collection with complex generic type argument: $L", collectionType.getTypeName());
      // Declare a null variable to satisfy return value requirement
      code.addStatement("$T $L = null", Collection.class, resultVarName);
      return resultVarName;
    }

    // Determine the appropriate collection implementation
    ClassName collectionImpl;
    Type collectionInterface;
    if (Set.class.isAssignableFrom((Class<?>) rawCollectionType)) {
      collectionImpl = HASH_SET;
      collectionInterface = Set.class;
      resultVarName = "level" + level + "Set";
    } else { // Default to List/ArrayList
      collectionImpl = ARRAY_LIST;
      collectionInterface = java.util.List.class;
      resultVarName = "level" + level + "List";
    }

    String arrayVar = "level" + level + "Array";
    String itemVar = "level" + level + "Item";

    // 1. Get the JSON Array
    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONArrayHandle(), arrayVar, accessExpression);
    // 2. Create the Collection instance
    code.addStatement("final $T<$T> $L = new $T<>()", collectionInterface, elementType, resultVarName, collectionImpl);
    // 3. Loop over the JSON Array
    // Assuming forEach supplies the item in a usable format (JSONObjectHandle, primitive wrapper, etc.)
    code.add("$L.forEach($L -> {\n", arrayVar, itemVar)
        .indent();

    // 4. Dispatch parsing for the element type
    // The access expression for the element is just the loop variable 'itemVar'
    String elementVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
        code,
        elementType,
        null, // No parent JSON object variable needed if itemVar holds the data directly
        parserPackage,
        CodeBlock.of("$L", itemVar), // Access the element via the loop variable
        level + 1);

    // 5. Add the parsed element to the collection
    code.addStatement("$L.add($L)", resultVarName, elementVarName);

    // 6. End loop
    code.unindent()
        .addStatement("})");

    return resultVarName;
  }

  // Handles Object E[] (e.g., String[], CustomObject[])
  private String generateObjectArrayParsingCodeInto(CodeBlock.Builder code, Class<?> arrayClass, String objVarName, String parserPackage,
      CodeBlock accessExpression, int level) {
    Class<?> componentType = arrayClass.getComponentType();
    String resultVarName = "level" + level + "Array";
    String arrayJsonVar = "level" + level + "JsonArray";
    String itemVar = "level" + level + "Item";
    String indexVar = "level" + level + "Index";

    // 1. Get the JSON Array
    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONArrayHandle(), arrayJsonVar, accessExpression);
    // 2. Create the Java Array instance
    code.addStatement("final $T[] $L = new $T[$L.length()]",
        componentType, resultVarName, componentType, arrayJsonVar);
    // 3. Loop over the JSON Array
    code.add("$L.forEach(($L, $L) -> {\n", arrayJsonVar, itemVar, indexVar)
        .indent();

    // 4. Dispatch parsing for the component type
    String elementVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
        code,
        componentType,
        null,
        parserPackage,
        CodeBlock.of("$L", itemVar),
        level + 1);

    // 5. Assign the parsed element to the array index
    code.addStatement("$L[$L] = $L", resultVarName, indexVar, elementVarName);

    // 6. End loop
    code.unindent()
        .addStatement("})");

    return resultVarName;
  }

  // Handles GenericArrayType (e.g., T[]) - Experimental
  private String generateGenericArrayParsingCodeInto(CodeBlock.Builder code, java.lang.reflect.GenericArrayType arrayType, String objVarName,
      String parserPackage, CodeBlock accessExpression, int level) {
    Type componentType = arrayType.getGenericComponentType();
    String resultVarName = "level" + level + "Array";
    String arrayJsonVar = "level" + level + "JsonArray";
    String itemVar = "level" + level + "Item";
    String indexVar = "level" + level + "Index";
    String tempListVar = "level" + level + "TempList"; // Need intermediate list

    // Cannot directly create generic array T[]. Create List first, then convert.
    // 1. Get the JSON Array
    code.addStatement("final $T $L = $L", ParserCommonUtils.getJSONArrayHandle(), arrayJsonVar, accessExpression);
    // 2. Create intermediate List
    code.addStatement("final $T<$T> $L = new $T<>()", java.util.List.class, componentType, tempListVar, java.util.ArrayList.class);

    // 3. Loop over JSON Array, parse elements into List
    code.add("$L.forEach($L -> {\n", arrayJsonVar, itemVar)
        .indent();
    String elementVarName = ParserWriterUtils.dispatchGenerateParsingCodeInto(
        code,
        componentType,
        null,
        parserPackage,
        CodeBlock.of("$L", itemVar),
        level + 1);
    code.addStatement("$L.add($L)", tempListVar, elementVarName);
    code.unindent()
        .addStatement("})");

    // 4. Convert List to Array (This requires reflection or assumptions about componentType)
    //    This part is complex and might not be universally solvable without runtime class info.
    //    For now, we'll generate a comment and assign null.
    code.addStatement("// TODO: Convert $L to array of $L", tempListVar, componentType.getTypeName());
    code.addStatement("final $T[] $L = null; // Cannot directly create generic array", Object.class, resultVarName);

    return resultVarName;
  }

  // --- Deprecated FieldParser methods ---
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

  // --- Deprecated TypeParser methods (returning CodeBlock) ---
  @Override
  @Deprecated
  public CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage) {
    return generateParsingCode(type, objVarName, parserPackage, "value"); // Use a default name
  }

  @Override
  @Deprecated
  public CodeBlock generateParsingCode(Type type, String objVarName, String parserPackage, String fieldName) {
    // This deprecated method is hard to map directly to the new recursive style,
    // as it assumed direct assignment to a config object.
    // We will generate the value retrieval into a variable and add a comment.
    CodeBlock.Builder code = CodeBlock.builder();
    // Collections/Arrays are accessed via getArray
    CodeBlock accessExpression = ParserCommonUtils.createFieldAccessCode(type, objVarName, CodeBlock.of("$S", fieldName));

    code.add(ParserCommonUtils.createFieldExistsCheck(objVarName, fieldName, true, innerCode -> {
      // Note: The level is hardcoded to 1 here, which might not be correct if this
      // deprecated method were ever called in a nested context (which it shouldn't be).
      String resultVar = generateParsingCodeInto(innerCode, type, objVarName, parserPackage, accessExpression, 1);
      // Placeholder for assignment
      innerCode.addStatement("// Assign result using deprecated method: config.set$L($L);", ParserCommonUtils.capitalize(fieldName), resultVar);
    }));
    return code.build();
  }
}