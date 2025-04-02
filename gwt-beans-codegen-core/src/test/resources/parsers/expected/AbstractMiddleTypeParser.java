package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.AbstractMiddleType;
import nl.aerius.wui.service.json.JSONObjectHandle;

public class AbstractMiddleTypeParser {
  public static AbstractMiddleType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static AbstractMiddleType parse(JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    // Cannot instantiate abstract class
    throw new UnsupportedOperationException("Cannot create an instance of an abstract class");
  }

  public static void parse(JSONObjectHandle obj, AbstractMiddleType config) {
    if (obj == null) {
      return;
    }

    // Parse fields from parent class (BaseType)
    BaseTypeParser.parse(obj, config);

    // Parse middle
    if (obj.has("middle")) {
      config.setMiddle(obj.getString("middle"));
    }
  }
}