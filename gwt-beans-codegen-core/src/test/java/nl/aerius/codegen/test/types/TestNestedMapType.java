package nl.aerius.codegen.test.types;

import java.util.HashMap;
import java.util.Map;

public class TestNestedMapType {
  // Simple nested map: Map<String, Map<String, Integer>>
  private Map<String, Map<String, Integer>> stringToNestedIntMap = new HashMap<>();

  // Complex nested map: Map<String, Map<String, Map<String, Double>>>
  private Map<String, Map<String, Map<String, Double>>> deeplyNestedDoubleMap = new HashMap<>();

  // Mixed nested map: Map<String, Map<Integer, Map<String, Boolean>>>
  private Map<String, Map<Integer, Map<String, Boolean>>> mixedNestedMap = new HashMap<>();

  // Nested map with custom key type: Map<TestEnumType.Status, Map<String, Map<String, Integer>>>
  private Map<TestEnumType.Status, Map<String, Map<String, Integer>>> enumKeyNestedMap = new HashMap<>();

  // Nested map with 5 levels of nested maps: Map<String, Map<String, Map<String, Map<String, String>>>>
  private Map<String, Map<String, Map<String, Map<String, String>>>> deeplyNestedStringMap = new HashMap<>();

  public Map<String, Map<String, Integer>> getStringToNestedIntMap() {
    return stringToNestedIntMap;
  }

  public void setStringToNestedIntMap(Map<String, Map<String, Integer>> stringToNestedIntMap) {
    this.stringToNestedIntMap = stringToNestedIntMap;
  }

  public Map<String, Map<String, Map<String, Double>>> getDeeplyNestedDoubleMap() {
    return deeplyNestedDoubleMap;
  }

  public void setDeeplyNestedDoubleMap(Map<String, Map<String, Map<String, Double>>> deeplyNestedDoubleMap) {
    this.deeplyNestedDoubleMap = deeplyNestedDoubleMap;
  }

  public Map<String, Map<Integer, Map<String, Boolean>>> getMixedNestedMap() {
    return mixedNestedMap;
  }

  public void setMixedNestedMap(Map<String, Map<Integer, Map<String, Boolean>>> mixedNestedMap) {
    this.mixedNestedMap = mixedNestedMap;
  }

  public Map<TestEnumType.Status, Map<String, Map<String, Integer>>> getEnumKeyNestedMap() {
    return enumKeyNestedMap;
  }

  public void setEnumKeyNestedMap(Map<TestEnumType.Status, Map<String, Map<String, Integer>>> enumKeyNestedMap) {
    this.enumKeyNestedMap = enumKeyNestedMap;
  }

  public Map<String, Map<String, Map<String, Map<String, String>>>> getDeeplyNestedStringMap() {
    return deeplyNestedStringMap;
  }

  public void setDeeplyNestedStringMap(
      Map<String, Map<String, Map<String, Map<String, String>>>> deeplyNestedStringMap) {
    this.deeplyNestedStringMap = deeplyNestedStringMap;
  }

  public static TestNestedMapType createFullObject() {
    TestNestedMapType obj = new TestNestedMapType();

    // Fill stringToNestedIntMap
    Map<String, Map<String, Integer>> outerMap = new HashMap<>();
    Map<String, Integer> innerMap1 = new HashMap<>();
    innerMap1.put("key1", 1);
    innerMap1.put("key2", 2);
    outerMap.put("outer1", innerMap1);
    obj.setStringToNestedIntMap(outerMap);

    // Fill deeplyNestedDoubleMap
    Map<String, Map<String, Map<String, Double>>> deepMap = new HashMap<>();
    Map<String, Map<String, Double>> midMap = new HashMap<>();
    Map<String, Double> innerMap2 = new HashMap<>();
    innerMap2.put("value1", 1.1);
    innerMap2.put("value2", 2.2);
    midMap.put("mid1", innerMap2);
    deepMap.put("deep1", midMap);
    obj.setDeeplyNestedDoubleMap(deepMap);

    // Fill mixedNestedMap
    Map<String, Map<Integer, Map<String, Boolean>>> mixedMap = new HashMap<>();
    Map<Integer, Map<String, Boolean>> midMap2 = new HashMap<>();
    Map<String, Boolean> innerMap3 = new HashMap<>();
    innerMap3.put("bool1", true);
    innerMap3.put("bool2", false);
    midMap2.put(1, innerMap3);
    mixedMap.put("mixed1", midMap2);
    obj.setMixedNestedMap(mixedMap);

    // Fill enumKeyNestedMap
    Map<TestEnumType.Status, Map<String, Map<String, Integer>>> enumMap = new HashMap<>();
    Map<String, Map<String, Integer>> midMap3 = new HashMap<>();
    Map<String, Integer> innerMap4 = new HashMap<>();
    innerMap4.put("enumKey1", 42);
    midMap3.put("enumMid1", innerMap4);
    enumMap.put(TestEnumType.Status.ACTIVE, midMap3);
    obj.setEnumKeyNestedMap(enumMap);

    // Corrected deeplyNestedStringMap creation (5 levels)
    Map<String, Map<String, Map<String, Map<String, String>>>> deeplyNestedStringMap = new HashMap<>();
    Map<String, Map<String, Map<String, String>>> level2 = new HashMap<>();
    Map<String, Map<String, String>> level3 = new HashMap<>();
    Map<String, String> level4 = new HashMap<>();

    level4.put("level4Key", "value");
    level3.put("level3Key", level4);
    level2.put("level2Key", level3);
    deeplyNestedStringMap.put("level1Key", level2);
    obj.setDeeplyNestedStringMap(deeplyNestedStringMap);

    return obj;
  }

  public static TestNestedMapType createNullObject() {
    TestNestedMapType obj = new TestNestedMapType();
    obj.setStringToNestedIntMap(null);
    obj.setDeeplyNestedDoubleMap(null);
    obj.setMixedNestedMap(null);
    obj.setEnumKeyNestedMap(null);
    obj.setDeeplyNestedStringMap(null);
    return obj;
  }
}