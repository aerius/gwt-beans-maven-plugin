package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.BaseType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
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
    if (baseObj == null || config == null) {
      return;
    }
    
    // Parse deepest
    if (baseObj.has("deepest") && !baseObj.isNull("deepest")) {
      final String value = baseObj.getString("deepest");
      config.setDeepest(value);
    }
  }
} 