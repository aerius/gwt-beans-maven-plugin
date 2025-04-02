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
  
  public static BaseType parse(JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final BaseType config = new BaseType();
    parse(obj, config);
    return config;
  }
  
  public static void parse(JSONObjectHandle obj, BaseType config) {
    if (obj == null) {
      return;
    }
    
    // Parse fields specific to BaseType
    if (obj.has("deepest")) {
      config.setDeepest(obj.getString("deepest"));
    }
    
    // No parent class to parse (BaseType is at the top of the hierarchy)
  }
} 