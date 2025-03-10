package nl.aerius.codegen.test;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;

import nl.aerius.codegen.test.types.TestRootObjectType;
import nl.aerius.json.JSONObjectHandle;

public class ExpectedParserGenerator {
  private static final ClassName STRING = ClassName.get(String.class);
  private static final ClassName JSON_OBJECT_HANDLE = ClassName.get(JSONObjectHandle.class);
  private static final ClassName TEST_ROOT_OBJECT_TYPE = ClassName.get(TestRootObjectType.class);

  public static JavaFile generateExpectedParser() {
    TypeSpec parser = TypeSpec.classBuilder("TestRootObjectTypeParser")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", "nl.aerius.codegen.ParserGenerator")
            .addMember("date", "$S", "2024-01-01T00:00:00")
            .build())
        .addMethod(generateParseMethod())
        .build();

    return JavaFile.builder("nl.aerius.codegen.test.generated", parser)
        .skipJavaLangImports(true)
        .indent("  ")
        .build();
  }

  private static MethodSpec generateParseMethod() {
    return MethodSpec.methodBuilder("parse")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(TEST_ROOT_OBJECT_TYPE)
        .addParameter(STRING, "jsonText")
        .addCode("""
            if (jsonText == null) {
              return null;
            }

            final $T config = new $T();
            final $T obj = $T.fromText(jsonText);

            // Parse foo
            if (obj.has("foo")) {
              config.setFoo(obj.getString("foo"));
            }

            // Parse count
            if (obj.has("count")) {
              config.setCount(obj.getInteger("count"));
            }

            // Parse booleanField
            if (obj.has("booleanField")) {
              config.setBooleanField(obj.getBoolean("booleanField"));
            }

            // Parse byteField
            if (obj.has("byteField")) {
              config.setByteField((byte) obj.getInteger("byteField"));
            }

            // Parse shortField
            if (obj.has("shortField")) {
              config.setShortField((short) obj.getInteger("shortField"));
            }

            // Parse longField
            if (obj.has("longField")) {
              config.setLongField(obj.getLong("longField"));
            }

            // Parse floatField
            if (obj.has("floatField")) {
              config.setFloatField((float) obj.getNumber("floatField"));
            }

            // Parse doubleField
            if (obj.has("doubleField")) {
              config.setDoubleField(obj.getNumber("doubleField"));
            }

            // Parse charField
            if (obj.has("charField")) {
              config.setCharField(obj.getString("charField").charAt(0));
            }

            // Parse booleanObjectField
            if (obj.has("booleanObjectField")) {
              config.setBooleanObjectField(obj.getBoolean("booleanObjectField"));
            }

            // Parse byteObjectField
            if (obj.has("byteObjectField")) {
              config.setByteObjectField((byte) obj.getInteger("byteObjectField"));
            }

            // Parse shortObjectField
            if (obj.has("shortObjectField")) {
              config.setShortObjectField((short) obj.getInteger("shortObjectField"));
            }

            // Parse integerObjectField
            if (obj.has("integerObjectField")) {
              config.setIntegerObjectField(obj.getInteger("integerObjectField"));
            }

            // Parse longObjectField
            if (obj.has("longObjectField")) {
              config.setLongObjectField(obj.getLong("longObjectField"));
            }

            // Parse floatObjectField
            if (obj.has("floatObjectField")) {
              config.setFloatObjectField((float) obj.getNumber("floatObjectField"));
            }

            // Parse doubleObjectField
            if (obj.has("doubleObjectField")) {
              config.setDoubleObjectField(obj.getNumber("doubleObjectField"));
            }

            // Parse charObjectField
            if (obj.has("charObjectField")) {
              config.setCharObjectField(obj.getString("charObjectField").charAt(0));
            }

            // Parse primitiveIntArray
            if (obj.has("primitiveIntArray")) {
              final List<Integer> primitiveArray = obj.getIntegerArray("primitiveIntArray");
              final int[] result = new int[primitiveArray.size()];
              for (int i = 0; i < primitiveArray.size(); i++) {
                result[i] = primitiveArray.get(i);
              }
              config.setPrimitiveIntArray(result);
            }

            // Parse stringArray
            if (obj.has("stringArray")) {
              config.setStringArray(obj.getStringArray("stringArray").toArray(new String[0]));
            }

            // Parse doubleObjectArray
            if (obj.has("doubleObjectArray")) {
              final List<Double> doubleArray = obj.getNumberArray("doubleObjectArray");
              config.setDoubleObjectArray(doubleArray.toArray(new Double[0]));
            }

            // Parse stringList
            if (obj.has("stringList")) {
              config.setStringList(obj.getStringArray("stringList"));
            }

            // Parse integerSet
            if (obj.has("integerSet")) {
              config.setIntegerSet(new HashSet<>(obj.getIntegerArray("integerSet")));
            }

            // Parse doubleList
            if (obj.has("doubleList")) {
              config.setDoubleList(obj.getNumberArray("doubleList"));
            }

            // Parse stringToIntMap
            if (obj.has("stringToIntMap")) {
              final JSONObjectHandle mapObj = obj.getObject("stringToIntMap");
              final Map<String, Integer> map = new HashMap<>();
              mapObj.keySet().forEach(key -> map.put(key, mapObj.getInteger(key)));
              config.setStringToIntMap(map);
            }

            // Parse stringToDoubleListMap
            if (obj.has("stringToDoubleListMap")) {
              final JSONObjectHandle mapObj = obj.getObject("stringToDoubleListMap");
              final Map<String, List<Double>> map = new HashMap<>();
              mapObj.keySet().forEach(key -> map.put(key, mapObj.getNumberArray(key)));
              config.setStringToDoubleListMap(map);
            }

            return config;
            """, TEST_ROOT_OBJECT_TYPE, TEST_ROOT_OBJECT_TYPE, JSON_OBJECT_HANDLE, JSON_OBJECT_HANDLE)
        .build();
  }
}