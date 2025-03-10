package nl.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.processing.Generated;

import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestAdvancedMapType;
import nl.aerius.codegen.test.types.TestSimpleTypesType;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestAdvancedMapTypeParser {
  public static TestAdvancedMapType parse(String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestAdvancedMapType parse(JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestAdvancedMapType config = new TestAdvancedMapType();

    // Parse doubleListMap
    if (obj.has("doubleListMap") && !obj.isNull("doubleListMap")) {
      final JSONObjectHandle mapObj = obj.getObject("doubleListMap");
      final HashMap<String, List<Double>> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getNumberArray(key));
      });
      config.setDoubleListMap(map);
    }

    // Parse stringListMap
    if (obj.has("stringListMap") && !obj.isNull("stringListMap")) {
      final JSONObjectHandle mapObj = obj.getObject("stringListMap");
      final HashMap<String, List<String>> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getStringArray(key));
      });
      config.setStringListMap(map);
    }

    // Parse objectListMap
    if (obj.has("objectListMap") && !obj.isNull("objectListMap")) {
      final JSONObjectHandle mapObj = obj.getObject("objectListMap");
      final HashMap<String, List<TestSimpleTypesType>> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        final List<TestSimpleTypesType> list = new ArrayList<>();
        mapObj.getArray(key).forEach(item -> {
          list.add(TestSimpleTypesTypeParser.parse(item));
        });
        map.put(key, list);
      });
      config.setObjectListMap(map);
    }

    // Parse interfaceMap - this should be skipped as it has a complex interface
    // type
    // Skipping field with complex generic type: interfaceMap

    // Parse wildcardListMap - this should be skipped as it has a wildcard type
    // Skipping field with wildcard type parameters: wildcardListMap

    // Parse wildcardKeyMap - this should be skipped as it has a wildcard key type
    // Skipping field with wildcard type parameters: wildcardKeyMap

    // Parse sanity
    if (obj.has("sanity")) {
      config.setSanity(obj.getString("sanity"));
    }

    return config;
  }
}