package nl.aerius.codegen.test.types;

import nl.aerius.wui.service.json.JSONObjectHandle;

public class ConcreteTypeParser {
  public static ConcreteType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static ConcreteType parse(JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final ConcreteType config = new ConcreteType();
    parse(obj, config);
    return config;
  }

  public static void parse(JSONObjectHandle obj, ConcreteType config) {
    if (obj == null) {
      return;
    }

    // Parse fields from parent class (AbstractMiddleType)
    AbstractMiddleTypeParser.parse(obj, config);

    // Parse fields specific to ConcreteType
    if (obj.has("outer")) {
      config.setOuter(obj.getString("outer"));
    }
  }
}