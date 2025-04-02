package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;
import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestNestedMapType;
import nl.aerius.codegen.test.types.TestEnumType;
import java.util.HashMap;
import java.util.Map;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestNestedMapTypeParser {
  public static TestNestedMapType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestNestedMapType parse(final JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestNestedMapType config = new TestNestedMapType();
    parse(obj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle obj, final TestNestedMapType config) {
    if (obj == null) {
      return;
    }

    // Parse stringToNestedIntMap
    if (obj.has("stringToNestedIntMap")) {
      final JSONObjectHandle mapObj = obj.getObject("stringToNestedIntMap");
      final Map<String, Map<String, Integer>> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        final JSONObjectHandle valueObj = mapObj.getObject(key);
        final Map<String, Integer> innerMap = new HashMap<>();
        valueObj.keySet().forEach(innerKey -> {
          innerMap.put(innerKey, valueObj.getInteger(innerKey));
        });
        map.put(key, innerMap);
      });
      config.setStringToNestedIntMap(map);
    }

    // Parse deeplyNestedDoubleMap
    if (obj.has("deeplyNestedDoubleMap")) {
      final JSONObjectHandle mapObj = obj.getObject("deeplyNestedDoubleMap");
      final Map<String, Map<String, Map<String, Double>>> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        final JSONObjectHandle midObj = mapObj.getObject(key);
        final Map<String, Map<String, Double>> midMap = new HashMap<>();
        midObj.keySet().forEach(midKey -> {
          final JSONObjectHandle innerObj = midObj.getObject(midKey);
          final Map<String, Double> innerMap = new HashMap<>();
          innerObj.keySet().forEach(innerKey -> {
            innerMap.put(innerKey, innerObj.getNumber(innerKey));
          });
          midMap.put(midKey, innerMap);
        });
        map.put(key, midMap);
      });
      config.setDeeplyNestedDoubleMap(map);
    }

    // Parse mixedNestedMap
    if (obj.has("mixedNestedMap")) {
      final JSONObjectHandle mapObj = obj.getObject("mixedNestedMap");
      final Map<String, Map<Integer, Map<String, Boolean>>> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        final JSONObjectHandle midObj = mapObj.getObject(key);
        final Map<Integer, Map<String, Boolean>> midMap = new HashMap<>();
        midObj.keySet().forEach(midKey -> {
          final JSONObjectHandle innerObj = midObj.getObject(midKey);
          final Map<String, Boolean> innerMap = new HashMap<>();
          innerObj.keySet().forEach(innerKey -> {
            innerMap.put(innerKey, innerObj.getBoolean(innerKey));
          });
          midMap.put(Integer.parseInt(midKey), innerMap);
        });
        map.put(key, midMap);
      });
      config.setMixedNestedMap(map);
    }

    // Parse enumKeyNestedMap
    if (obj.has("enumKeyNestedMap")) {
      final JSONObjectHandle mapObj = obj.getObject("enumKeyNestedMap");
      final Map<TestEnumType.Status, Map<String, Map<String, Integer>>> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        final JSONObjectHandle midObj = mapObj.getObject(key);
        final Map<String, Map<String, Integer>> midMap = new HashMap<>();
        midObj.keySet().forEach(midKey -> {
          final JSONObjectHandle innerObj = midObj.getObject(midKey);
          final Map<String, Integer> innerMap = new HashMap<>();
          innerObj.keySet().forEach(innerKey -> {
            innerMap.put(innerKey, innerObj.getInteger(innerKey));
          });
          midMap.put(midKey, innerMap);
        });
        map.put(TestEnumType.Status.valueOf(key), midMap);
      });
      config.setEnumKeyNestedMap(map);
    }

    // Parse deeplyNestedStringMap
    if (obj.has("deeplyNestedStringMap")) {
      final JSONObjectHandle mapObj = obj.getObject("deeplyNestedStringMap");
      final Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>>> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        final JSONObjectHandle level2Obj = mapObj.getObject(key);
        final Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>> level2 = new HashMap<>();
        level2Obj.keySet().forEach(level2Key -> {
          final JSONObjectHandle level3Obj = level2Obj.getObject(level2Key);
          final Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>> level3 = new HashMap<>();
          level3Obj.keySet().forEach(level3Key -> {
            final JSONObjectHandle level4Obj = level3Obj.getObject(level3Key);
            final Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> level4 = new HashMap<>();
            level4Obj.keySet().forEach(level4Key -> {
              final JSONObjectHandle level5Obj = level4Obj.getObject(level4Key);
              final Map<String, Map<String, Map<String, Map<String, String>>>> level5 = new HashMap<>();
              level5Obj.keySet().forEach(level5Key -> {
                final JSONObjectHandle level6Obj = level5Obj.getObject(level5Key);
                final Map<String, Map<String, Map<String, String>>> level6 = new HashMap<>();
                level6Obj.keySet().forEach(level6Key -> {
                  final JSONObjectHandle level7Obj = level6Obj.getObject(level6Key);
                  final Map<String, Map<String, String>> level7 = new HashMap<>();
                  level7Obj.keySet().forEach(level7Key -> {
                    final JSONObjectHandle level8Obj = level7Obj.getObject(level7Key);
                    final Map<String, String> level8 = new HashMap<>();
                    level8Obj.keySet().forEach(level8Key -> {
                      level8.put(level8Key, level8Obj.getString(level8Key));
                    });
                    level7.put(level7Key, level8);
                  });
                  level6.put(level6Key, level7);
                });
                level5.put(level5Key, level6);
              });
              level4.put(level4Key, level5);
            });
            level3.put(level3Key, level4);
          });
          level2.put(level2Key, level3);
        });
        map.put(key, level2);
      });
      config.setDeeplyNestedStringMap(map);
    }
  }
}