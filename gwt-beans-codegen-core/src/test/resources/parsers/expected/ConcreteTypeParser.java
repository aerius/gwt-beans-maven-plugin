package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.ConcreteType;
import nl.aerius.wui.service.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class ConcreteTypeParser {
  public static ConcreteType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static ConcreteType parse(final JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final ConcreteType config = new ConcreteType();
    parse(obj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle obj, final ConcreteType config) {
    if (obj == null) {
      return;
    }

    // Parse fields from parent class (AbstractMiddleType)
    AbstractMiddleTypeParser.parse(obj, config);

    // Parse outer
    if (obj.has("outer")) {
      config.setOuter(obj.getString("outer"));
    }
  }
}