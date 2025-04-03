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

  public static TestComplexCollectionType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final TestComplexCollectionType config = new TestComplexCollectionType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestComplexCollectionType config) {
    if (baseObj == null) {
      return;
    }

    // Parse objectMap
    if (baseObj.has("objectMap") && !baseObj.isNull("objectMap")) {
      final JSONObjectHandle obj = baseObj.getObject("objectMap");
      final Map<String, TestSimpleTypesType> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONObjectHandle valueObj = obj.getObject(key);
        final TestSimpleTypesType value = TestSimpleTypesTypeParser.parse(valueObj);
        map.put(key, value);
      });
      config.setObjectMap(map);
    }

    // Parse sanity
    if (baseObj.has("sanity")) {
      config.setSanity(baseObj.getString("sanity"));
    }

    // Parse integerMap
    if (baseObj.has("integerMap") && !baseObj.isNull("integerMap")) {
      final JSONObjectHandle obj = baseObj.getObject("integerMap");
      final Map<String, Integer> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final Integer value = obj.getInteger(key);
        map.put(key, value);
      });
      config.setIntegerMap(map);
    }

    // Parse doubleMap
    if (baseObj.has("doubleMap") && !baseObj.isNull("doubleMap")) {
      final JSONObjectHandle obj = baseObj.getObject("doubleMap");
      final Map<String, Double> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final Double value = obj.getNumber(key);
        map.put(key, value);
      });
      config.setDoubleMap(map);
    }

    // Parse enumKeyStringMap
    if (baseObj.has("enumKeyStringMap") && !baseObj.isNull("enumKeyStringMap")) {
      final JSONObjectHandle obj = baseObj.getObject("enumKeyStringMap");
      final Map<TestEnumType.Status, String> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        final String value = obj.getString(key);
        map.put(enumKey, value);
      });
      config.setEnumKeyStringMap(map);
    }

    // Parse enumKeyObjectMap
    if (baseObj.has("enumKeyObjectMap") && !baseObj.isNull("enumKeyObjectMap")) {
      final JSONObjectHandle obj = baseObj.getObject("enumKeyObjectMap");
      final Map<TestEnumType.Status, TestSimpleTypesType> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONObjectHandle valueObj = obj.getObject(key);
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        final TestSimpleTypesType value = TestSimpleTypesTypeParser.parse(valueObj);
        map.put(enumKey, value);
      });
      config.setEnumKeyObjectMap(map);
    }

    // Parse enumKeyIntegerMap
    if (baseObj.has("enumKeyIntegerMap") && !baseObj.isNull("enumKeyIntegerMap")) {
      final JSONObjectHandle obj = baseObj.getObject("enumKeyIntegerMap");
      final Map<TestEnumType.Status, Integer> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        final Integer value = obj.getInteger(key);
        map.put(enumKey, value);
      });
      config.setEnumKeyIntegerMap(map);
    }

    // Parse integerKeyEnumMap
    if (baseObj.has("integerKeyEnumMap") && !baseObj.isNull("integerKeyEnumMap")) {
      final JSONObjectHandle obj = baseObj.getObject("integerKeyEnumMap");
      final Map<Integer, TestEnumType.Status> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final String level2Str = obj.getString(key);
        TestEnumType.Status level2Value = null;
        if (level2Str != null) {
          try {
            level2Value = TestEnumType.Status.valueOf(level2Str);
          } catch (IllegalArgumentException e) {
            // Invalid enum value, leave as default
          }
        }
        map.put(Integer.parseInt(key), level2Value);
      });
      config.setIntegerKeyEnumMap(map);
    }

    // Parse integerKeyObjectMap
    if (baseObj.has("integerKeyObjectMap") && !baseObj.isNull("integerKeyObjectMap")) {
      final JSONObjectHandle obj = baseObj.getObject("integerKeyObjectMap");
      final Map<Integer, TestSimpleTypesType> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final TestSimpleTypesType level2Value = TestSimpleTypesTypeParser.parse(obj.getObject(key));
        map.put(Integer.parseInt(key), level2Value);
      });
      config.setIntegerKeyObjectMap(map);
    }

  }
}
