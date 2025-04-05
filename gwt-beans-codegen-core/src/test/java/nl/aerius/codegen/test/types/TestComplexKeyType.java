package nl.aerius.codegen.test.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonKey;

/**
 * A complex type used as a map key in tests.
 * This class demonstrates the use of JsonKey and JsonCreator annotations
 * for serialization and deserialization of complex map keys.
 */
public class TestComplexKeyType {
  private String name;
  private int value;

  public TestComplexKeyType() {}

  public TestComplexKeyType(String name, int value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  /**
   * Converts this object to a string representation for JSON serialization.
   * This method is used when this object is used as a map key.
   */
  @JsonKey
  public String toStringValue() {
    return name + ":" + value;
  }

  /**
   * Creates an instance from a string representation.
   * This method is used when deserializing this object from a map key.
   */
  @JsonCreator
  public static TestComplexKeyType fromStringValue(String str) {
    String[] parts = str.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid format: " + str);
    }
    return new TestComplexKeyType(parts[0], Integer.parseInt(parts[1]));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TestComplexKeyType that = (TestComplexKeyType) o;
    return value == that.value && name.equals(that.name);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + value;
    return result;
  }

  @Override
  public String toString() {
    return "TestComplexKeyType{" +
        "name='" + name + '\'' +
        ", value=" + value +
        '}';
  }
}