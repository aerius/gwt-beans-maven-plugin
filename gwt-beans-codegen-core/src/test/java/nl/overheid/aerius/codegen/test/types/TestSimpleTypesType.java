package nl.overheid.aerius.codegen.test.types;

/**
 * Test class for simple types that are supported in the parser generator but
 * not fully tested.
 * This class is a subtype of TestRootObjectType to keep the main test classes
 * clean.
 */
public class TestSimpleTypesType {
  // Primitive types
  private byte primitiveByte;
  private short primitiveShort;
  private float primitiveFloat;
  private char primitiveChar;
  private long primitiveLong;

  // Wrapper types
  private Byte wrapperByte;
  private Short wrapperShort;
  private Float wrapperFloat;
  private Character wrapperChar;
  private Long wrapperLong;

  // Getters and setters
  public byte getPrimitiveByte() {
    return primitiveByte;
  }

  public void setPrimitiveByte(byte primitiveByte) {
    this.primitiveByte = primitiveByte;
  }

  public short getPrimitiveShort() {
    return primitiveShort;
  }

  public void setPrimitiveShort(short primitiveShort) {
    this.primitiveShort = primitiveShort;
  }

  public float getPrimitiveFloat() {
    return primitiveFloat;
  }

  public void setPrimitiveFloat(float primitiveFloat) {
    this.primitiveFloat = primitiveFloat;
  }

  public char getPrimitiveChar() {
    return primitiveChar;
  }

  public void setPrimitiveChar(char primitiveChar) {
    this.primitiveChar = primitiveChar;
  }

  public long getPrimitiveLong() {
    return primitiveLong;
  }

  public void setPrimitiveLong(long primitiveLong) {
    this.primitiveLong = primitiveLong;
  }

  public Byte getWrapperByte() {
    return wrapperByte;
  }

  public void setWrapperByte(Byte wrapperByte) {
    this.wrapperByte = wrapperByte;
  }

  public Short getWrapperShort() {
    return wrapperShort;
  }

  public void setWrapperShort(Short wrapperShort) {
    this.wrapperShort = wrapperShort;
  }

  public Float getWrapperFloat() {
    return wrapperFloat;
  }

  public void setWrapperFloat(Float wrapperFloat) {
    this.wrapperFloat = wrapperFloat;
  }

  public Character getWrapperChar() {
    return wrapperChar;
  }

  public void setWrapperChar(Character wrapperChar) {
    this.wrapperChar = wrapperChar;
  }

  public Long getWrapperLong() {
    return wrapperLong;
  }

  public void setWrapperLong(Long wrapperLong) {
    this.wrapperLong = wrapperLong;
  }

  /**
   * Creates a fully populated instance with test values.
   */
  public static TestSimpleTypesType createFullObject() {
    TestSimpleTypesType obj = new TestSimpleTypesType();

    // Set primitive values
    obj.setPrimitiveByte((byte) 42);
    obj.setPrimitiveShort((short) 1000);
    obj.setPrimitiveFloat(3.14f);
    obj.setPrimitiveChar('A');
    obj.setPrimitiveLong(9876543210L);

    // Set wrapper values
    obj.setWrapperByte(Byte.valueOf((byte) 127));
    obj.setWrapperShort(Short.valueOf((short) 32000));
    obj.setWrapperFloat(Float.valueOf(2.718f));
    obj.setWrapperChar(Character.valueOf('Z'));
    obj.setWrapperLong(Long.valueOf(1234567890L));

    return obj;
  }

  /**
   * Creates an instance with null values where possible.
   * Primitive types will have their default values.
   */
  public static TestSimpleTypesType createNullObject() {
    TestSimpleTypesType obj = new TestSimpleTypesType();
    // Primitive types will have default values (0, 0.0, '\u0000', etc.)
    // Set all wrapper types to null
    obj.setWrapperByte(null);
    obj.setWrapperShort(null);
    obj.setWrapperFloat(null);
    obj.setWrapperChar(null);
    obj.setWrapperLong(null);

    return obj;
  }
}