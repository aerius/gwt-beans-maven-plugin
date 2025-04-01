package nl.aerius.codegen.test.types;

import java.util.ArrayList;
import java.util.List;

import nl.aerius.codegen.test.types.TestEnumType.Status;

/**
 * Test type for validating List&lt;Enum&gt; parsing.
 */
public class TestEnumListType {

  private List<Status> statusList;
  private String description; // Add another field for basic object structure

  public List<Status> getStatusList() {
    return statusList;
  }

  public void setStatusList(List<Status> statusList) {
    this.statusList = statusList;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Creates a fully populated instance with test values.
   */
  public static TestEnumListType createFullObject() {
    TestEnumListType obj = new TestEnumListType();
    obj.setDescription("Full Enum List Test");
    List<Status> enums = new ArrayList<>();
    enums.add(Status.ACTIVE);
    enums.add(Status.PENDING);
    enums.add(Status.COMPLETED);
    obj.setStatusList(enums);
    return obj;
  }

  /**
   * Creates an instance with null values where possible.
   */
  public static TestEnumListType createNullObject() {
    TestEnumListType obj = new TestEnumListType();
    obj.setDescription(null);
    obj.setStatusList(null);
    return obj;
  }
}