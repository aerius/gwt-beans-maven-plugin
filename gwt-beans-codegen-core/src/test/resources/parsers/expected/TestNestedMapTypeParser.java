package nl.aerius.codegen.test.generated;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import nl.aerius.codegen.test.types.TestEnumType;
import nl.aerius.codegen.test.types.TestNestedMapType;
import nl.aerius.wui.service.json.JSONObjectHandle;

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
    if (obj.has("stringToNestedIntMap") && !obj.isNull("stringToNestedIntMap")) {
      final JSONObjectHandle level1Obj = obj.getObject("stringToNestedIntMap");
      final Map<String, Map<String, Integer>> level1Map = new LinkedHashMap<>();
      level1Obj.keySet().forEach(level1Key -> {
        final JSONObjectHandle level2Obj = level1Obj.getObject(level1Key);
        final Map<String, Integer> level2Map = new LinkedHashMap<>();
        level2Obj.keySet().forEach(level2Key -> {
          final Integer level3Value = level2Obj.getInteger(level2Key);
          level2Map.put(level2Key, level3Value);
        });
        level1Map.put(level1Key, level2Map);
      });
      config.setStringToNestedIntMap(level1Map);
    }

    // Parse deeplyNestedDoubleMap
    if (obj.has("deeplyNestedDoubleMap") && !obj.isNull("deeplyNestedDoubleMap")) {
      final JSONObjectHandle level1Obj = obj.getObject("deeplyNestedDoubleMap");
      final Map<String, Map<String, Map<String, Double>>> level1Map = new LinkedHashMap<>();
      level1Obj.keySet().forEach(level1Key -> {
        final JSONObjectHandle level2Obj = level1Obj.getObject(level1Key);
        final Map<String, Map<String, Double>> level2Map = new LinkedHashMap<>();
        level2Obj.keySet().forEach(level2Key -> {
          final JSONObjectHandle level3Obj = level2Obj.getObject(level2Key);
          final Map<String, Double> level3Map = new LinkedHashMap<>();
          level3Obj.keySet().forEach(level3Key -> {
            final Double level4Value = level3Obj.getNumber(level3Key);
            level3Map.put(level3Key, level4Value);
          });
          level2Map.put(level2Key, level3Map);
        });
        level1Map.put(level1Key, level2Map);
      });
      config.setDeeplyNestedDoubleMap(level1Map);
    }

    // Parse mixedNestedMap
    if (obj.has("mixedNestedMap") && !obj.isNull("mixedNestedMap")) {
      final JSONObjectHandle level1Obj = obj.getObject("mixedNestedMap");
      final Map<String, Map<Integer, Map<String, Boolean>>> level1Map = new LinkedHashMap<>();
      level1Obj.keySet().forEach(level1Key -> {
        final JSONObjectHandle level2Obj = level1Obj.getObject(level1Key);
        final Map<Integer, Map<String, Boolean>> level2Map = new LinkedHashMap<>();
        level2Obj.keySet().forEach(level2Key -> {
          final JSONObjectHandle level3Obj = level2Obj.getObject(level2Key);
          final Map<String, Boolean> level3Map = new LinkedHashMap<>();
          level3Obj.keySet().forEach(level3Key -> {
            final Boolean level4Value = level3Obj.getBoolean(level3Key);
            level3Map.put(level3Key, level4Value);
          });
          level2Map.put(Integer.parseInt(level2Key), level3Map);
        });
        level1Map.put(level1Key, level2Map);
      });
      config.setMixedNestedMap(level1Map);
    }

    // Parse enumKeyNestedMap
    if (obj.has("enumKeyNestedMap") && !obj.isNull("enumKeyNestedMap")) {
      final JSONObjectHandle level1Obj = obj.getObject("enumKeyNestedMap");
      final Map<TestEnumType.Status, Map<String, Map<String, Integer>>> level1Map = new LinkedHashMap<>();
      level1Obj.keySet().forEach(level1Key -> {
        final JSONObjectHandle level2Obj = level1Obj.getObject(level1Key);
        final Map<String, Map<String, Integer>> level2Map = new LinkedHashMap<>();
        level2Obj.keySet().forEach(level2Key -> {
          final JSONObjectHandle level3Obj = level2Obj.getObject(level2Key);
          final Map<String, Integer> level3Map = new LinkedHashMap<>();
          level3Obj.keySet().forEach(level3Key -> {
            final Integer level4Value = level3Obj.getInteger(level3Key);
            level3Map.put(level3Key, level4Value);
          });
          level2Map.put(level2Key, level3Map);
        });
        level1Map.put(TestEnumType.Status.valueOf(level1Key), level2Map);
      });
      config.setEnumKeyNestedMap(level1Map);
    }

    // Parse deeplyNestedStringMap
    if (obj.has("deeplyNestedStringMap") && !obj.isNull("deeplyNestedStringMap")) {
      final JSONObjectHandle level1Obj = obj.getObject("deeplyNestedStringMap");
      final Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>>> level1Map = new LinkedHashMap<>();
      level1Obj.keySet().forEach(level1Key -> {
        final JSONObjectHandle level2Obj = level1Obj.getObject(level1Key);
        final Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>> level2Map = new LinkedHashMap<>();
        level2Obj.keySet().forEach(level2Key -> {
          final JSONObjectHandle level3Obj = level2Obj.getObject(level2Key);
          final Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>> level3Map = new LinkedHashMap<>();
          level3Obj.keySet().forEach(level3Key -> {
            final JSONObjectHandle level4Obj = level3Obj.getObject(level3Key);
            final Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> level4Map = new LinkedHashMap<>();
            level4Obj.keySet().forEach(level4Key -> {
              final JSONObjectHandle level5Obj = level4Obj.getObject(level4Key);
              final Map<String, Map<String, Map<String, Map<String, String>>>> level5Map = new LinkedHashMap<>();
              level5Obj.keySet().forEach(level5Key -> {
                final JSONObjectHandle level6Obj = level5Obj.getObject(level5Key);
                final Map<String, Map<String, Map<String, String>>> level6Map = new LinkedHashMap<>();
                level6Obj.keySet().forEach(level6Key -> {
                  final JSONObjectHandle level7Obj = level6Obj.getObject(level6Key);
                  final Map<String, Map<String, String>> level7Map = new LinkedHashMap<>();
                  level7Obj.keySet().forEach(level7Key -> {
                    final JSONObjectHandle level8Obj = level7Obj.getObject(level7Key);
                    final Map<String, String> level8Map = new LinkedHashMap<>();
                    level8Obj.keySet().forEach(level8Key -> {
                      final String level9value = level8Obj.getString(level8Key);
                      level8Map.put(level8Key, level9value);
                    });
                    level7Map.put(level7Key, level8Map);
                  });
                  level6Map.put(level6Key, level7Map);
                });
                level5Map.put(level5Key, level6Map);
              });
              level4Map.put(level4Key, level5Map);
            });
            level3Map.put(level3Key, level4Map);
          });
          level2Map.put(level2Key, level3Map);
        });
        level1Map.put(level1Key, level2Map);
      });
      config.setDeeplyNestedStringMap(level1Map);
    }
  }
}