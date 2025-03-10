package nl.aerius.codegen.test.types;

/**
 * Test class for enum types that should be supported in the parser generator.
 */
public class TestEnumType {
  /**
   * Test enum representing different status values.
   */
  public enum Status {
    ACTIVE,
    INACTIVE,
    PENDING,
    COMPLETED
  }

  /**
   * Test enum representing different priority levels.
   */
  public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  // Enum fields
  private Status status;
  private Priority priority;
  private Status nullableStatus;

  // Getters and setters
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public Status getNullableStatus() {
    return nullableStatus;
  }

  public void setNullableStatus(Status nullableStatus) {
    this.nullableStatus = nullableStatus;
  }

  /**
   * Creates a fully populated instance with test values.
   */
  public static TestEnumType createFullObject() {
    TestEnumType obj = new TestEnumType();
    obj.setStatus(Status.ACTIVE);
    obj.setPriority(Priority.HIGH);
    obj.setNullableStatus(Status.PENDING);
    return obj;
  }

  /**
   * Creates an instance with null values where possible.
   * Non-nullable fields will have default values.
   */
  public static TestEnumType createNullObject() {
    TestEnumType obj = new TestEnumType();
    // Set required fields
    obj.setStatus(Status.INACTIVE);
    obj.setPriority(Priority.LOW);
    // Set nullable fields to null
    obj.setNullableStatus(null);
    return obj;
  }
}