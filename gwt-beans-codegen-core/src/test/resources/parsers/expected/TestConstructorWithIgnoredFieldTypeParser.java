package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestConstructorWithIgnoredFieldType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestConstructorWithIgnoredFieldTypeParser {
  public static TestConstructorWithIgnoredFieldType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestConstructorWithIgnoredFieldType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    // Parse name
    if (!baseObj.has("name")) {
      throw new RuntimeException("Required field 'name' is missing");
    }
    final String name = baseObj.getString("name");

    return new TestConstructorWithIgnoredFieldType(name);
  }
}
