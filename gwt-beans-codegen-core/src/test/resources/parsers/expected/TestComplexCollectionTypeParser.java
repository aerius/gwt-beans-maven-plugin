package nl.aerius.codegen.test.generated;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestComplexCollectionType;
import nl.aerius.codegen.test.types.TestEnumType;
import nl.aerius.codegen.test.types.TestSimpleTypesType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestComplexCollectionTypeParser {
  public static TestComplexCollectionType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestComplexCollectionType parse(final JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestComplexCollectionType config = new TestComplexCollectionType();
    parse(obj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle obj, final TestComplexCollectionType config) {
    if (obj == null) {
      return;
    }

    // Parse objectMap
    if (obj.has("objectMap") && !obj.isNull("objectMap")) {
      final JSONObjectHandle mapObj = obj.getObject("objectMap");
      final Map<String, TestSimpleTypesType> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final JSONObjectHandle valueObj = mapObj.getObject(key);
        map.put(key, TestSimpleTypesTypeParser.parse(valueObj));
      });
      config.setObjectMap(map);
    }

    // Parse sanity
    if (obj.has("sanity")) {
      config.setSanity(obj.getString("sanity"));
    }

    // Parse integerMap
    if (obj.has("integerMap") && !obj.isNull("integerMap")) {
      final JSONObjectHandle mapObj = obj.getObject("integerMap");
      final Map<String, Integer> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getInteger(key));
      });
      config.setIntegerMap(map);
    }

    // Parse doubleMap
    if (obj.has("doubleMap") && !obj.isNull("doubleMap")) {
      final JSONObjectHandle mapObj = obj.getObject("doubleMap");
      final Map<String, Double> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getNumber(key));
      });
      config.setDoubleMap(map);
    }

    // Parse enumKeyStringMap
    if (obj.has("enumKeyStringMap") && !obj.isNull("enumKeyStringMap")) {
      final JSONObjectHandle mapObj = obj.getObject("enumKeyStringMap");
      final Map<TestEnumType.Status, String> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, mapObj.getString(key));
      });
      config.setEnumKeyStringMap(map);
    }

    // Parse enumKeyObjectMap
    if (obj.has("enumKeyObjectMap") && !obj.isNull("enumKeyObjectMap")) {
      final JSONObjectHandle mapObj = obj.getObject("enumKeyObjectMap");
      final Map<TestEnumType.Status, TestSimpleTypesType> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final JSONObjectHandle valueObj = mapObj.getObject(key);
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, TestSimpleTypesTypeParser.parse(valueObj));
      });
      config.setEnumKeyObjectMap(map);
    }

    // Parse enumKeyIntegerMap
    if (obj.has("enumKeyIntegerMap") && !obj.isNull("enumKeyIntegerMap")) {
      final JSONObjectHandle mapObj = obj.getObject("enumKeyIntegerMap");
      final Map<TestEnumType.Status, Integer> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, mapObj.getInteger(key));
      });
      config.setEnumKeyIntegerMap(map);
    }

    // Parse integerKeyEnumMap
    if (obj.has("integerKeyEnumMap") && !obj.isNull("integerKeyEnumMap")) {
      final JSONObjectHandle mapObj = obj.getObject("integerKeyEnumMap");
      final Map<Integer, TestEnumType.Status> level1Map = new LinkedHashMap<>();
      mapObj.keySet().forEach(level1Key -> {
        final Integer intKey = Integer.parseInt(level1Key);
        final String level2Str = mapObj.getString(level1Key);
        TestEnumType.Status level2Value = null;
        if (level2Str != null) {
          try {
            level2Value = TestEnumType.Status.valueOf(level2Str);
          } catch (IllegalArgumentException e) {
            System.err.println("Warning: Invalid integerKeyEnumMap value enum: " + level2Str);
          }
        }
        level1Map.put(intKey, level2Value);
      });
      config.setIntegerKeyEnumMap(level1Map);
    }

    // Parse integerKeyObjectMap
    if (obj.has("integerKeyObjectMap") && !obj.isNull("integerKeyObjectMap")) {
      final JSONObjectHandle level1Obj = obj.getObject("integerKeyObjectMap");
      final Map<Integer, TestSimpleTypesType> level1Map = new LinkedHashMap<>();
      level1Obj.keySet().forEach(level1Key -> {
        final TestSimpleTypesType level2Value = TestSimpleTypesTypeParser.parse(level1Obj.getObject(level1Key));
        level1Map.put(Integer.parseInt(level1Key), level2Value);
      });
      config.setIntegerKeyObjectMap(level1Map);
    }

  }
}
