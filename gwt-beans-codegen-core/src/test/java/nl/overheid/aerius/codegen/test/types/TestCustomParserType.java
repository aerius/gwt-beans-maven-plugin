package nl.overheid.aerius.codegen.test.types;

/**
 * Test class that will have a custom parser implementation.
 * This class is used to test that custom parsers are used instead of generated
 * ones.
 */
public class TestCustomParserType {
  private String customField;
  private int customValue;

  public String getCustomField() {
    return customField;
  }

  public void setCustomField(String customField) {
    this.customField = customField;
  }

  public int getCustomValue() {
    return customValue;
  }

  public void setCustomValue(int customValue) {
    this.customValue = customValue;
  }

  /**
   * Creates a fully populated instance with test values.
   */
  public static TestCustomParserType createFullObject() {
    TestCustomParserType obj = new TestCustomParserType();
    obj.setCustomField("custom parser test");
    obj.setCustomValue(42);
    return obj;
  }

  /**
   * Creates an instance with null values where possible.
   * Primitive types will have their default values.
   */
  public static TestCustomParserType createNullObject() {
    TestCustomParserType obj = new TestCustomParserType();
    obj.setCustomField(null);
    // customValue will have default value (0)
    return obj;
  }
}