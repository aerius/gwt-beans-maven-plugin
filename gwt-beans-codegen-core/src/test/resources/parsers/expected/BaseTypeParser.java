package nl.aerius.codegen.test.generated;

import nl.aerius.codegen.test.types.BaseType;
import nl.aerius.json.JSONObjectHandle;

public class BaseTypeParser {
  public static BaseType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }
  
  public static BaseType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final BaseType config = new BaseType();
    parse(baseObj, config);
    return config;
  }
  
  public static void parse(final JSONObjectHandle baseObj, final BaseType config) {
    if (baseObj == null) {
      return;
    }
    
    // Parse deepest
    if (baseObj.has("deepest") && !baseObj.isNull("deepest")) {
      final String value = baseObj.getString("deepest");
      config.setDeepest(value);
    }
  }
} 