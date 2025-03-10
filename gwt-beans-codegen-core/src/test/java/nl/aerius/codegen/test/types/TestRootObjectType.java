package nl.aerius.codegen.test.types;

import java.util.Map;

public class TestRootObjectType {
  private String foo;
  private int count;
  private boolean active;

  private TestSimpleCollectionType simpleCollection;
  private TestSimpleTypesType simpleTypes;
  private TestCustomParserType customParserType;
  private TestEnumType enumType;
  private TestComplexCollectionType complexCollection;

  public String getFoo() {
    return foo;
  }

  public void setFoo(String foo) {
    this.foo = foo;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public TestSimpleCollectionType getSimpleCollection() {
    return simpleCollection;
  }

  public void setSimpleCollection(TestSimpleCollectionType simpleCollection) {
    this.simpleCollection = simpleCollection;
  }

  public TestSimpleTypesType getSimpleTypes() {
    return simpleTypes;
  }

  public void setSimpleTypes(TestSimpleTypesType simpleTypes) {
    this.simpleTypes = simpleTypes;
  }

  public TestCustomParserType getCustomParserType() {
    return customParserType;
  }

  public void setCustomParserType(TestCustomParserType customParserType) {
    this.customParserType = customParserType;
  }

  public TestEnumType getEnumType() {
    return enumType;
  }

  public void setEnumType(TestEnumType enumType) {
    this.enumType = enumType;
  }

  public TestComplexCollectionType getComplexCollection() {
    return complexCollection;
  }

  public void setComplexCollection(TestComplexCollectionType complexCollection) {
    this.complexCollection = complexCollection;
  }

  public static TestRootObjectType createFullObject() {
    TestRootObjectType obj = new TestRootObjectType();
    obj.setFoo("test string");
    obj.setCount(42);
    obj.setActive(true);
    obj.setSimpleCollection(TestSimpleCollectionType.createFullObject());
    obj.setSimpleTypes(TestSimpleTypesType.createFullObject());
    obj.setCustomParserType(TestCustomParserType.createFullObject());
    obj.setEnumType(TestEnumType.createFullObject());
    obj.setComplexCollection(createTestComplexCollection());
    return obj;
  }

  private static TestComplexCollectionType createTestComplexCollection() {
    TestComplexCollectionType test = new TestComplexCollectionType();
    test.setSanity("test");

    TestSimpleTypesType obj1 = new TestSimpleTypesType();
    obj1.setPrimitiveByte((byte) 1);
    obj1.setPrimitiveShort((short) 2);
    obj1.setPrimitiveFloat(3.14f);
    obj1.setPrimitiveChar('A');
    obj1.setPrimitiveLong(123L);

    TestSimpleTypesType obj2 = new TestSimpleTypesType();
    obj2.setPrimitiveByte((byte) 4);
    obj2.setPrimitiveShort((short) 5);
    obj2.setPrimitiveFloat(6.28f);
    obj2.setPrimitiveChar('B');
    obj2.setPrimitiveLong(456L);

    test.setObjectMap(Map.of("first", obj1, "second", obj2));
    return test;
  }

  /**
   * Creates an instance with null values where possible.
   * Primitive types will have their default values.
   */
  public static TestRootObjectType createNullObject() {
    TestRootObjectType obj = new TestRootObjectType();
    obj.setFoo(null);
    // Primitives will have default values (0, false)
    obj.setSimpleCollection(null);
    obj.setSimpleTypes(null);
    obj.setCustomParserType(null);
    obj.setEnumType(null);
    obj.setComplexCollection(null);
    return obj;
  }
}
