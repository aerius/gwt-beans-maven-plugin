package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestConstructorBasedType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestConstructorBasedTypeParser {
  public static TestConstructorBasedType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestConstructorBasedType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    // Parse name
    if (!baseObj.has("name")) {
      throw new RuntimeException("Required field 'name' is missing");
    }
    final String name;
    if (!baseObj.isNull("name")) {
      name = baseObj.getString("name");
    } else {
      name = null;
    }

    // Parse value
    if (!baseObj.has("value")) {
      throw new RuntimeException("Required field 'value' is missing");
    }
    final int value = baseObj.getInteger("value");

    // Parse optionalValue
    if (!baseObj.has("optionalValue")) {
      throw new RuntimeException("Required field 'optionalValue' is missing");
    }
    final Double optionalValue;
    if (!baseObj.isNull("optionalValue")) {
      optionalValue = baseObj.getNumber("optionalValue");
    } else {
      optionalValue = null;
    }

    return new TestConstructorBasedType(name, value, optionalValue);
  }
}
