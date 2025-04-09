package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.AbstractMiddleType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class AbstractMiddleTypeParser {
  public static AbstractMiddleType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static AbstractMiddleType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    throw new UnsupportedOperationException("Cannot directly instantiate abstract class or interface " + AbstractMiddleType.class.getName() + ". Use @JsonTypeInfo or a custom parser.");
  }

  public static void parse(final JSONObjectHandle baseObj, final AbstractMiddleType config) {
    if (baseObj == null || config == null) {
      return;
    }

    // Parse fields from parent class (BaseType)
    BaseTypeParser.parse(baseObj, config);

    // Parse middle
    if (baseObj.has("middle") && !baseObj.isNull("middle")) {
      final String value = baseObj.getString("middle");
      config.setMiddle(value);
    }
  }
}