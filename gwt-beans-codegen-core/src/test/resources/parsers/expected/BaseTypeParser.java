package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.BaseType;
import nl.aerius.wui.service.json.JSONObjectHandle;

public class BaseTypeParser {
  public static BaseType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }
  
  public static BaseType parse(final JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final BaseType config = new BaseType();
    parse(obj, config);
    return config;
  }
  
  public static void parse(final JSONObjectHandle obj, final BaseType config) {
    if (obj == null) {
      return;
    }
    
    // Parse deepest
    if (obj.has("deepest")) {
      config.setDeepest(obj.getString("deepest"));
    }
  }
} 