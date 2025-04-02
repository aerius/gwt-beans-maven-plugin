package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestEnumType;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestEnumTypeParser {
  public static TestEnumType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestEnumType parse(final JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestEnumType config = new TestEnumType();
    parse(obj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle obj, final TestEnumType config) {
    if (obj == null) {
      return;
    }

    // Parse status
    if (obj.has("status") && !obj.isNull("status")) {
      final String statusStr = obj.getString("status");
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
      final String priorityStr = obj.getString("priority");
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
      final String level1Str = obj.getString("nullableStatus");
      TestEnumType.Status level1Value = null;
      if (level1Str != null) {
        try {
          level1Value = TestEnumType.Status.valueOf(level1Str);
        } catch (IllegalArgumentException e) {
          // Match generated comment
          // Invalid enum value "[level1Str]", leaving level1Value as null;
        }
      }
      config.setNullableStatus(level1Value);
    }
  }
}
