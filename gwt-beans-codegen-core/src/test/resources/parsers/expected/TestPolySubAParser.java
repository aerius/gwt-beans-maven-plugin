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
    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestPolySubA parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    TestPolySubA instance = new TestPolySubA();
    parse(baseObj, instance);
    return instance;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestPolySubA config) {
    if (baseObj == null || config == null) {
      return;
    }

    // Parse fields from parent class (TestPolyBase)
    TestPolyBaseParser.parse(baseObj, config);

    // Parse fieldB
    if (baseObj.has("fieldA")) {
      final int value = baseObj.getInteger("fieldA");
      config.setFieldA(value);
    }
  }
}