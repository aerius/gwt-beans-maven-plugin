package nl.aerius.codegen.test.types;

/**
 * Test class for constructor-based parsing.
 * This class has no setters - all field values must be provided via the constructor.
 * The parser generator should detect this and generate a constructor-based parser.
 */
public class TestConstructorBasedType {
  private final String name;
  private final int value;
  private final Double optionalValue;

  public TestConstructorBasedType(String name, int value, Double optionalValue) {
    this.name = name;
    this.value = value;
    this.optionalValue = optionalValue;
  }

  // Getters only, NO setters
  public String getName() {
    return name;
  }

  public int getValue() {
    return value;
  }

  public Double getOptionalValue() {
    return optionalValue;
  }

  /**
   * Creates a fully populated instance with test values.
   */
  public static TestConstructorBasedType createFullObject() {
    return new TestConstructorBasedType("test", 42, 3.14);
  }

  /**
   * Creates an instance with null values where possible.
   * Primitive types will have their default values.
   */
  public static TestConstructorBasedType createNullObject() {
    return new TestConstructorBasedType(null, 0, null);
  }
}
