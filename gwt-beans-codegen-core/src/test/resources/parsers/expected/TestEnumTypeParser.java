package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestEnumType;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestEnumTypeParser {
  public static TestEnumType parse(String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestEnumType parse(JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestEnumType config = new TestEnumType();

    // Parse status
    if (obj.has("status") && !obj.isNull("status")) {
      String statusStr = obj.getString("status");
      if (statusStr != null) {
        try {
          config.setStatus(TestEnumType.Status.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
          // Invalid enum value, leave as default
        }
      }
    }

    // Parse priority
    if (obj.has("priority") && !obj.isNull("priority")) {
      String priorityStr = obj.getString("priority");
      if (priorityStr != null) {
        try {
          config.setPriority(TestEnumType.Priority.valueOf(priorityStr));
        } catch (IllegalArgumentException e) {
          // Invalid enum value, leave as default
        }
      }
    }

    // Parse nullableStatus
    if (obj.has("nullableStatus") && !obj.isNull("nullableStatus")) {
      String nullableStatusStr = obj.getString("nullableStatus");
      if (nullableStatusStr != null) {
        try {
          config.setNullableStatus(TestEnumType.Status.valueOf(nullableStatusStr));
        } catch (IllegalArgumentException e) {
          // Invalid enum value, leave as default
        }
      }
    }

    return config;
  }
}