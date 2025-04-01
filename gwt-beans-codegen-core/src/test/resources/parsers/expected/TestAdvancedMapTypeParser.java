package nl.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.processing.Generated;

import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestAdvancedMapType;
import nl.aerius.codegen.test.types.TestSimpleTypesType;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestAdvancedMapTypeParser {
  public static TestAdvancedMapType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestAdvancedMapType parse(final JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestAdvancedMapType config = new TestAdvancedMapType();

    // Parse doubleListMap
    if (obj.has("doubleListMap") && !obj.isNull("doubleListMap")) {
      final JSONObjectHandle mapObj = obj.getObject("doubleListMap");
      final LinkedHashMap<String, List<Double>> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getNumberArray(key));
      });
      config.setDoubleListMap(map);
    }

    // Parse stringListMap
    if (obj.has("stringListMap") && !obj.isNull("stringListMap")) {
      final JSONObjectHandle mapObj = obj.getObject("stringListMap");
      final LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getStringArray(key));
      });
      config.setStringListMap(map);
    }

    // Parse objectListMap
    if (obj.has("objectListMap") && !obj.isNull("objectListMap")) {
      final JSONObjectHandle mapObj = obj.getObject("objectListMap");
      final LinkedHashMap<String, List<TestSimpleTypesType>> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        final List<TestSimpleTypesType> list = new ArrayList<>();
        mapObj.getArray(key).forEach(item -> {
          list.add(TestSimpleTypesTypeParser.parse(item));
        });
        map.put(key, list);
      });
      config.setObjectListMap(map);
    }

    // Parse interfaceMap
    // Skipping field with complex generic type: interfaceMap

    // Parse wildcardListMap
    // Skipping field with complex generic type: wildcardListMap

    // Parse wildcardKeyMap
    // Skipping field with complex generic type: wildcardKeyMap

    // Parse sanity
    if (obj.has("sanity")) {
      config.setSanity(obj.getString("sanity"));
    }

    return config;
  }
}