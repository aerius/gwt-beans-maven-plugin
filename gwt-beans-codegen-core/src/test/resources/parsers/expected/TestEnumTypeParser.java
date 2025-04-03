package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestEnumType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestEnumTypeParser {
  public static TestEnumType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestEnumType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final TestEnumType config = new TestEnumType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestEnumType config) {
    if (baseObj == null) {
      return;
    }

    // Parse status
    if (baseObj.has("status") && !baseObj.isNull("status")) {
      final String statusStr = baseObj.getString("status");
      if (statusStr != null) {
        try {
          config.setStatus(TestEnumType.Status.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
          // Invalid enum value, leave as default
        }
      }
    }

    // Parse priority
    if (baseObj.has("priority") && !baseObj.isNull("priority")) {
      final String priorityStr = baseObj.getString("priority");
      if (priorityStr != null) {
        try {
          config.setPriority(TestEnumType.Priority.valueOf(priorityStr));
        } catch (IllegalArgumentException e) {
          // Invalid enum value, leave as default
        }
      }
    }

    // Parse nullableStatus
    if (baseObj.has("nullableStatus") && !baseObj.isNull("nullableStatus")) {
      final String str = baseObj.getString("nullableStatus");
      TestEnumType.Status value = null;
      if (str != null) {
        try {
          value = TestEnumType.Status.valueOf(str);
        } catch (IllegalArgumentException e) {
          // Invalid enum value "[str]", leaving value as null
        }
      }
      config.setNullableStatus(value);
    }
  }
}
