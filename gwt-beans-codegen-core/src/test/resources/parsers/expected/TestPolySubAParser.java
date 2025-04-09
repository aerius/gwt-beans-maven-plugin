package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.polymorphic.TestPolySubA;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "GENERATION_TIMESTAMP")
public class TestPolySubAParser {

  public static TestPolySubA parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }
    JSONObjectHandle handle = JSONObjectHandle.fromText(jsonText);
    return parse(handle);
  }

  public static TestPolySubA parse(final JSONObjectHandle handle) {
    if (handle == null) {
      return null;
    }
    TestPolySubA instance = new TestPolySubA();
    parse(handle, instance);
    return instance;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestPolySubA config) {
    if (baseObj == null) {
      return;
    }

    // Parse fields from parent class (TestPolyBase)
    TestPolyBaseParser.config(baseObj, config);

    // Parse fieldB
    if (baseObj.has("fieldA")) {
      final int value = baseObj.getInteger("fieldA");
      config.setFieldA(value);
    }
  }
}