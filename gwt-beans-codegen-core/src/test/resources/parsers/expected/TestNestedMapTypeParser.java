package nl.aerius.codegen.test.generated;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestEnumType;
import nl.aerius.codegen.test.types.TestNestedMapType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestNestedMapTypeParser {
  public static TestNestedMapType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }
    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestNestedMapType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }
    final TestNestedMapType config = new TestNestedMapType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestNestedMapType config) {
    if (baseObj == null || config == null) {
      return;
    }

    // Parse stringToNestedIntMap
    if (baseObj.has("stringToNestedIntMap") && !baseObj.isNull("stringToNestedIntMap")) {
      final JSONObjectHandle obj = baseObj.getObject("stringToNestedIntMap");
      final Map<String, Map<String, Integer>> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONObjectHandle level2Obj = obj.getObject(key);
        final Map<String, Integer> level2Map = new LinkedHashMap<>();
        level2Obj.keySet().forEach(level2Key -> {
          final Integer level3Value = level2Obj.getInteger(level2Key);
          level2Map.put(level2Key, level3Value);
        });
        map.put(key, level2Map);
      });
      config.setStringToNestedIntMap(map);
    }

    // Parse deeplyNestedDoubleMap
    if (baseObj.has("deeplyNestedDoubleMap") && !baseObj.isNull("deeplyNestedDoubleMap")) {
      final JSONObjectHandle obj = baseObj.getObject("deeplyNestedDoubleMap");
      final Map<String, Map<String, Map<String, Double>>> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONObjectHandle level2Obj = obj.getObject(key);
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
        map.put(key, level2Map);
      });
      config.setDeeplyNestedDoubleMap(map);
    }

    // Parse mixedNestedMap
    if (baseObj.has("mixedNestedMap") && !baseObj.isNull("mixedNestedMap")) {
      final JSONObjectHandle obj = baseObj.getObject("mixedNestedMap");
      final Map<String, Map<Integer, Map<String, Boolean>>> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONObjectHandle level2Obj = obj.getObject(key);
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
        map.put(key, level2Map);
      });
      config.setMixedNestedMap(map);
    }

    // Parse enumKeyNestedMap
    if (baseObj.has("enumKeyNestedMap") && !baseObj.isNull("enumKeyNestedMap")) {
      final JSONObjectHandle obj = baseObj.getObject("enumKeyNestedMap");
      final Map<TestEnumType.Status, Map<String, Map<String, Integer>>> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONObjectHandle level2Obj = obj.getObject(key);
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
        final TestEnumType.Status enumKey = TestEnumType.Status.valueOf(key);
        map.put(enumKey, level2Map);
      });
      config.setEnumKeyNestedMap(map);
    }

    // Parse deeplyNestedStringMap
    if (baseObj.has("deeplyNestedStringMap") && !baseObj.isNull("deeplyNestedStringMap")) {
      final JSONObjectHandle obj = baseObj.getObject("deeplyNestedStringMap");
      final Map<String, Map<String, Map<String, Map<String, String>>>> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final JSONObjectHandle level2Obj = obj.getObject(key);
        final Map<String, Map<String, Map<String, String>>> level2Map = new LinkedHashMap<>();
        level2Obj.keySet().forEach(level2Key -> {
          final JSONObjectHandle level3Obj = level2Obj.getObject(level2Key);
          final Map<String, Map<String, String>> level3Map = new LinkedHashMap<>();
          level3Obj.keySet().forEach(level3Key -> {
            final JSONObjectHandle level4Obj = level3Obj.getObject(level3Key);
            final Map<String, String> level4Map = new LinkedHashMap<>();
            level4Obj.keySet().forEach(level4Key -> {
              final String level5Value = level4Obj.getString(level4Key);
              level4Map.put(level4Key, level5Value);
            });
            level3Map.put(level3Key, level4Map);
          });
          level2Map.put(level2Key, level3Map);
        });
        map.put(key, level2Map);
      });
      config.setDeeplyNestedStringMap(map);
    }
  }
}