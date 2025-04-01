package nl.aerius.codegen.test.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.aerius.codegen.test.types.TestEnumType.Status;

/**
 * Test type for validating List&lt;Enum&gt; parsing.
 */
public class TestEnumListType {

  private List<Status> statusList;
  private Set<Status> statusSet;
  private Map<String, Status> statusMap;

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

  public Set<Status> getStatusSet() {
    return statusSet;
  }

  public void setStatusSet(Set<Status> statusSet) {
    this.statusSet = statusSet;
  }

  public Map<String, Status> getStatusMap() {
    return statusMap;
  }

  public void setStatusMap(Map<String, Status> statusMap) {
    this.statusMap = statusMap;
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