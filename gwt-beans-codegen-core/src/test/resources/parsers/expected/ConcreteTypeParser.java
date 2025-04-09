package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.ConcreteType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class ConcreteTypeParser {
  public static ConcreteType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static ConcreteType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final ConcreteType config = new ConcreteType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final ConcreteType config) {
    if (baseObj == null || config == null) {
      return;
    }

    // Parse fields from parent class (AbstractMiddleType)
    AbstractMiddleTypeParser.parse(baseObj, config);

    // Parse outer
    if (baseObj.has("outer") && !baseObj.isNull("outer")) {
      final String value = baseObj.getString("outer");
      config.setOuter(value);
    }
  }
}