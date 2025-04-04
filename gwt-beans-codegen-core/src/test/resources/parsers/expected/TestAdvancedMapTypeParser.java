package nl.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestAdvancedMapType;
import nl.aerius.codegen.test.types.TestComplexKeyType;
import nl.aerius.codegen.test.types.TestSimpleTypesType;
import nl.aerius.json.JSONArrayHandle;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestAdvancedMapTypeParser {
  public static TestAdvancedMapType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestAdvancedMapType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final TestAdvancedMapType config = new TestAdvancedMapType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestAdvancedMapType config) {
    if (baseObj == null) {
      return;
    }

    // Parse doubleListMap
    if (baseObj.has("doubleListMap") && !baseObj.isNull("doubleListMap")) {
      final JSONObjectHandle obj = baseObj.getObject("doubleListMap");
      final Map<String, List<Double>> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONArrayHandle level2Array = obj.getArray(key);
        final List<Double> level2List = new ArrayList<>();
        level2Array.forEachNumber(level2List::add);
        map.put(key, level2List);
      });
      config.setDoubleListMap(map);
    }

    // Parse stringListMap
    if (baseObj.has("stringListMap") && !baseObj.isNull("stringListMap")) {
      final JSONObjectHandle obj = baseObj.getObject("stringListMap");
      final Map<String, List<String>> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONArrayHandle level2Array = obj.getArray(key);
        final List<String> level2List = new ArrayList<>();
        level2Array.forEachString(level2List::add);
        map.put(key, level2List);
      });
      config.setStringListMap(map);
    }

    // Parse objectListMap
    if (baseObj.has("objectListMap") && !baseObj.isNull("objectListMap")) {
      final JSONObjectHandle obj = baseObj.getObject("objectListMap");
      final Map<String, List<TestSimpleTypesType>> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONArrayHandle level2Array = obj.getArray(key);
        final List<TestSimpleTypesType> level2List = new ArrayList<>();
        level2Array.forEach(level2Item -> {
          final TestSimpleTypesType level3Value = TestSimpleTypesTypeParser.parse(level2Item);
          level2List.add(level3Value);
        });
        map.put(key, level2List);
      });
      config.setObjectListMap(map);
    }

    // Skipping ignored field: interfaceMap

    // Parse complexKeyMap
    if (baseObj.has("complexKeyMap") && !baseObj.isNull("complexKeyMap")) {
      final JSONObjectHandle obj = baseObj.getObject("complexKeyMap");
      final Map<TestComplexKeyType, Double> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final Double level2Value = obj.getNumber(key);
        map.put(TestComplexKeyType.fromStringValue(key), level2Value);
      });
      config.setComplexKeyMap(map);
    }

    // Skipping ignored field: wildcardListMap

    // Skipping ignored field: wildcardKeyMap

    // Parse sanity
    if (baseObj.has("sanity") && !baseObj.isNull("sanity")) {
      final String value = baseObj.getString("sanity");
      config.setSanity(value);
    }

  }
}
