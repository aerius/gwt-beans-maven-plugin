package nl.aerius.codegen.test.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TestAdvancedMapType {
  // Map with collection values
  private Map<String, List<Double>> doubleListMap;
  private Map<String, List<String>> stringListMap;
  private Map<String, List<TestSimpleTypesType>> objectListMap;

  // Map with interface type values
  @JsonIgnore
  private Map<String, Comparable<?>> interfaceMap;

  // Map with wildcard types (should be skipped by the parser)
  @JsonIgnore
  private Map<String, List<?>> wildcardListMap;
  @JsonIgnore
  private Map<?, String> wildcardKeyMap;

  // Control field
  private String sanity;

  public Map<String, List<Double>> getDoubleListMap() {
    return doubleListMap;
  }

  public void setDoubleListMap(Map<String, List<Double>> doubleListMap) {
    this.doubleListMap = doubleListMap;
  }

  public Map<String, List<String>> getStringListMap() {
    return stringListMap;
  }

  public void setStringListMap(Map<String, List<String>> stringListMap) {
    this.stringListMap = stringListMap;
  }

  public Map<String, List<TestSimpleTypesType>> getObjectListMap() {
    return objectListMap;
  }

  public void setObjectListMap(Map<String, List<TestSimpleTypesType>> objectListMap) {
    this.objectListMap = objectListMap;
  }

  public Map<String, Comparable<?>> getInterfaceMap() {
    return interfaceMap;
  }

  public void setInterfaceMap(Map<String, Comparable<?>> interfaceMap) {
    this.interfaceMap = interfaceMap;
  }

  public Map<String, List<?>> getWildcardListMap() {
    return wildcardListMap;
  }

  public void setWildcardListMap(Map<String, List<?>> wildcardListMap) {
    this.wildcardListMap = wildcardListMap;
  }

  public Map<?, String> getWildcardKeyMap() {
    return wildcardKeyMap;
  }

  public void setWildcardKeyMap(Map<?, String> wildcardKeyMap) {
    this.wildcardKeyMap = wildcardKeyMap;
  }

  public String getSanity() {
    return sanity;
  }

  public void setSanity(String sanity) {
    this.sanity = sanity;
  }

  /**
   * Creates a fully populated instance with test values.
   */
  public static TestAdvancedMapType createFullObject() {
    TestAdvancedMapType test = new TestAdvancedMapType();
    test.setSanity("test");

    // Set up double list map
    Map<String, List<Double>> doubleListMap = new HashMap<>();
    List<Double> doubleList1 = new ArrayList<>();
    doubleList1.add(1.1);
    doubleList1.add(2.2);
    List<Double> doubleList2 = new ArrayList<>();
    doubleList2.add(3.3);
    doubleList2.add(4.4);
    doubleListMap.put("list1", doubleList1);
    doubleListMap.put("list2", doubleList2);
    test.setDoubleListMap(doubleListMap);

    // Set up string list map
    Map<String, List<String>> stringListMap = new HashMap<>();
    List<String> stringList1 = new ArrayList<>();
    stringList1.add("a");
    stringList1.add("b");
    List<String> stringList2 = new ArrayList<>();
    stringList2.add("c");
    stringList2.add("d");
    stringListMap.put("list1", stringList1);
    stringListMap.put("list2", stringList2);
    test.setStringListMap(stringListMap);

    // Set up object list map
    Map<String, List<TestSimpleTypesType>> objectListMap = new HashMap<>();
    List<TestSimpleTypesType> objectList = new ArrayList<>();
    objectList.add(createSimpleType((byte) 1, (short) 2, 3.14f, 'A', 123L));
    objectList.add(createSimpleType((byte) 4, (short) 5, 6.28f, 'B', 456L));
    objectListMap.put("objects", objectList);
    test.setObjectListMap(objectListMap);

    // Set up interface map
    Map<String, Comparable<?>> interfaceMap = new HashMap<>();
    interfaceMap.put("string", "test");
    interfaceMap.put("integer", 42);
    test.setInterfaceMap(interfaceMap);

    // Set up wildcard maps (these should be skipped by the parser)
    Map<String, List<?>> wildcardListMap = new HashMap<>();
    wildcardListMap.put("mixed", List.of("string", 42, 3.14));
    test.setWildcardListMap(wildcardListMap);

    Map<Object, String> wildcardKeyMap = new HashMap<>();
    wildcardKeyMap.put(42, "integer key");
    wildcardKeyMap.put("string", "string key");
    test.setWildcardKeyMap((Map) wildcardKeyMap);

    return test;
  }

  private static TestSimpleTypesType createSimpleType(byte b, short s, float f, char c, long l) {
    TestSimpleTypesType obj = new TestSimpleTypesType();
    obj.setPrimitiveByte(b);
    obj.setPrimitiveShort(s);
    obj.setPrimitiveFloat(f);
    obj.setPrimitiveChar(c);
    obj.setPrimitiveLong(l);
    return obj;
  }

  /**
   * Creates an instance with null values where possible.
   */
  public static TestAdvancedMapType createNullObject() {
    TestAdvancedMapType test = new TestAdvancedMapType();
    test.setSanity(null);
    test.setDoubleListMap(null);
    test.setStringListMap(null);
    test.setObjectListMap(null);
    test.setInterfaceMap(null);
    test.setWildcardListMap(null);
    test.setWildcardKeyMap(null);
    return test;
  }
}