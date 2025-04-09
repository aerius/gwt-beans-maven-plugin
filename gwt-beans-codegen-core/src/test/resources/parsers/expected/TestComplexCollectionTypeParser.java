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
    if (baseObj == null || config == null) {
      return;
    }

    // Parse objectMap
    if (baseObj.has("objectMap") && !baseObj.isNull("objectMap")) {
      final JSONObjectHandle obj = baseObj.getObject("objectMap");
      final Map<String, TestSimpleTypesType> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final TestSimpleTypesType level2Value = TestSimpleTypesTypeParser.parse(obj.getObject(key));
        map.put(key, level2Value);
      });
      config.setObjectMap(map);
    }

    // Parse sanity
    if (baseObj.has("sanity") && !baseObj.isNull("sanity")) {
      final String value = baseObj.getString("sanity");
      config.setSanity(value);
    }

    // Parse integerMap
    if (baseObj.has("integerMap") && !baseObj.isNull("integerMap")) {
      final JSONObjectHandle obj = baseObj.getObject("integerMap");
      final Map<String, Integer> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final Integer level2Value = obj.getInteger(key);
        map.put(key, level2Value);
      });
      config.setIntegerMap(map);
    }

    // Parse doubleMap
    if (baseObj.has("doubleMap") && !baseObj.isNull("doubleMap")) {
      final JSONObjectHandle obj = baseObj.getObject("doubleMap");
      final Map<String, Double> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final Double level2Value = obj.getNumber(key);
        map.put(key, level2Value);
      });
      config.setDoubleMap(map);
    }

    // Parse enumKeyStringMap
    if (baseObj.has("enumKeyStringMap") && !baseObj.isNull("enumKeyStringMap")) {
      final JSONObjectHandle obj = baseObj.getObject("enumKeyStringMap");
      final Map<TestEnumType.Status, String> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final String level2Value = obj.getString(key);
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, level2Value);
      });
      config.setEnumKeyStringMap(map);
    }

    // Parse enumKeyObjectMap
    if (baseObj.has("enumKeyObjectMap") && !baseObj.isNull("enumKeyObjectMap")) {
      final JSONObjectHandle obj = baseObj.getObject("enumKeyObjectMap");
      final Map<TestEnumType.Status, TestSimpleTypesType> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final TestSimpleTypesType level2Value = TestSimpleTypesTypeParser.parse(obj.getObject(key));
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, level2Value);
      });
      config.setEnumKeyObjectMap(map);
    }

    // Parse enumKeyIntegerMap
    if (baseObj.has("enumKeyIntegerMap") && !baseObj.isNull("enumKeyIntegerMap")) {
      final JSONObjectHandle obj = baseObj.getObject("enumKeyIntegerMap");
      final Map<TestEnumType.Status, Integer> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final Integer level2Value = obj.getInteger(key);
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, level2Value);
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
