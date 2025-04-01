package nl.aerius.codegen.test.generated;

import java.util.LinkedHashMap;

import javax.annotation.processing.Generated;

import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestComplexCollectionType;
import nl.aerius.codegen.test.types.TestEnumType;
import nl.aerius.codegen.test.types.TestSimpleTypesType;

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

    // Parse objectMap
    if (obj.has("objectMap") && !obj.isNull("objectMap")) {
      final JSONObjectHandle mapObj = obj.getObject("objectMap");
      final LinkedHashMap<String, TestSimpleTypesType> map = new LinkedHashMap<>();
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
      final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getInteger(key));
      });
      config.setIntegerMap(map);
    }

    // Parse doubleMap
    if (obj.has("doubleMap") && !obj.isNull("doubleMap")) {
      final JSONObjectHandle mapObj = obj.getObject("doubleMap");
      final LinkedHashMap<String, Double> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getNumber(key));
      });
      config.setDoubleMap(map);
    }

    // Parse enumKeyStringMap
    if (obj.has("enumKeyStringMap") && !obj.isNull("enumKeyStringMap")) {
      final JSONObjectHandle mapObj = obj.getObject("enumKeyStringMap");
      final LinkedHashMap<TestEnumType.Status, String> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, mapObj.getString(key));
      });
      config.setEnumKeyStringMap(map);
    }

    // Parse enumKeyObjectMap
    if (obj.has("enumKeyObjectMap") && !obj.isNull("enumKeyObjectMap")) {
      final JSONObjectHandle mapObj = obj.getObject("enumKeyObjectMap");
      final LinkedHashMap<TestEnumType.Status, TestSimpleTypesType> map = new LinkedHashMap<>();
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
      final LinkedHashMap<TestEnumType.Status, Integer> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, mapObj.getInteger(key));
      });
      config.setEnumKeyIntegerMap(map);
    }

    // Parse integerKeyEnumMap
    if (obj.has("integerKeyEnumMap") && !obj.isNull("integerKeyEnumMap")) {
      final JSONObjectHandle mapObj = obj.getObject("integerKeyEnumMap");
      final LinkedHashMap<Integer, TestEnumType.Status> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final Integer intKey = Integer.parseInt(key);
        map.put(intKey, TestEnumType.Status.valueOf(mapObj.getString(key)));
      });
      config.setIntegerKeyEnumMap(map);
    }

    // Parse integerKeyObjectMap
    if (obj.has("integerKeyObjectMap") && !obj.isNull("integerKeyObjectMap")) {
      final JSONObjectHandle mapObj = obj.getObject("integerKeyObjectMap");
      final LinkedHashMap<Integer, TestSimpleTypesType> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final Integer intKey = Integer.parseInt(key);
        final JSONObjectHandle valueObj = mapObj.getObject(key);
        map.put(intKey, TestSimpleTypesTypeParser.parse(valueObj));
      });
      config.setIntegerKeyObjectMap(map);
    }

    return config;
  }
}