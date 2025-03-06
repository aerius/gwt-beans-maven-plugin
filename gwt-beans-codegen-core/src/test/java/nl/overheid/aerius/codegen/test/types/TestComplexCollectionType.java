package nl.overheid.aerius.codegen.test.types;

import java.util.Map;

public class TestComplexCollectionType {
  private Map<String, TestSimpleTypesType> objectMap;
  private String sanity;

  // Test Map<String, primitive wrapper>
  private Map<String, Integer> integerMap;
  private Map<String, Double> doubleMap;

  // Test Map<Enum, X> variants
  private Map<TestEnumType.Status, String> enumKeyStringMap;
  private Map<TestEnumType.Status, TestSimpleTypesType> enumKeyObjectMap;
  private Map<TestEnumType.Status, Integer> enumKeyIntegerMap;

  public Map<String, TestSimpleTypesType> getObjectMap() {
    return objectMap;
  }

  public void setObjectMap(Map<String, TestSimpleTypesType> objectMap) {
    this.objectMap = objectMap;
  }

  public String getSanity() {
    return sanity;
  }

  public void setSanity(String sanity) {
    this.sanity = sanity;
  }

  public Map<String, Integer> getIntegerMap() {
    return integerMap;
  }

  public void setIntegerMap(Map<String, Integer> integerMap) {
    this.integerMap = integerMap;
  }

  public Map<String, Double> getDoubleMap() {
    return doubleMap;
  }

  public void setDoubleMap(Map<String, Double> doubleMap) {
    this.doubleMap = doubleMap;
  }

  public Map<TestEnumType.Status, String> getEnumKeyStringMap() {
    return enumKeyStringMap;
  }

  public void setEnumKeyStringMap(Map<TestEnumType.Status, String> enumKeyStringMap) {
    this.enumKeyStringMap = enumKeyStringMap;
  }

  public Map<TestEnumType.Status, TestSimpleTypesType> getEnumKeyObjectMap() {
    return enumKeyObjectMap;
  }

  public void setEnumKeyObjectMap(Map<TestEnumType.Status, TestSimpleTypesType> enumKeyObjectMap) {
    this.enumKeyObjectMap = enumKeyObjectMap;
  }

  public Map<TestEnumType.Status, Integer> getEnumKeyIntegerMap() {
    return enumKeyIntegerMap;
  }

  public void setEnumKeyIntegerMap(Map<TestEnumType.Status, Integer> enumKeyIntegerMap) {
    this.enumKeyIntegerMap = enumKeyIntegerMap;
  }

  /**
   * Creates a fully populated instance with test values.
   */
  public static TestComplexCollectionType createFullObject() {
    TestComplexCollectionType test = new TestComplexCollectionType();
    test.setSanity("test");

    // Set up original objectMap
    Map<String, TestSimpleTypesType> objectMap = Map.of(
        "first", createSimpleType((byte) 1, (short) 2, 3.14f, 'A', 123L),
        "second", createSimpleType((byte) 4, (short) 5, 6.28f, 'B', 456L));
    test.setObjectMap(objectMap);

    // Set up primitive wrapper maps
    test.setIntegerMap(Map.of("one", 1, "two", 2, "three", 3));
    test.setDoubleMap(Map.of("pi", 3.14, "e", 2.718));

    // Set up enum key maps
    test.setEnumKeyStringMap(Map.of(
        TestEnumType.Status.ACTIVE, "first value",
        TestEnumType.Status.PENDING, "second value"));

    test.setEnumKeyIntegerMap(Map.of(
        TestEnumType.Status.ACTIVE, 100,
        TestEnumType.Status.PENDING, 200));

    // Set up enum key with object values
    test.setEnumKeyObjectMap(Map.of(
        TestEnumType.Status.ACTIVE, createSimpleType((byte) 10, (short) 20, 1.1f, 'X', 1000L),
        TestEnumType.Status.PENDING, createSimpleType((byte) 30, (short) 40, 2.2f, 'Y', 2000L)));

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
   * Primitive types will have their default values.
   */
  public static TestComplexCollectionType createNullObject() {
    TestComplexCollectionType test = new TestComplexCollectionType();
    test.setSanity(null);
    test.setObjectMap(null);
    test.setIntegerMap(null);
    test.setDoubleMap(null);
    test.setEnumKeyStringMap(null);
    test.setEnumKeyObjectMap(null);
    test.setEnumKeyIntegerMap(null);
    return test;
  }
}