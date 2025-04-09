// This file serves as the reference implementation for validation (t2)
// and is executed by the expected roundtrip test (t1).
package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.polymorphic.TestPolySubB;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "GENERATION_TIMESTAMP")
public class TestPolySubBParser {

  public static TestPolySubB parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }
    JSONObjectHandle handle = JSONObjectHandle.fromText(jsonText);
    return parse(handle);
  }

  // Creates instance and calls the populating parse method
  public static TestPolySubB parse(final JSONObjectHandle handle) {
    if (handle == null) {
      return null;
    }
    TestPolySubB instance = new TestPolySubB();
    parse(handle, instance);
    return instance;
  }

  // Populates an existing instance
  public static void parse(final JSONObjectHandle baseObj, final TestPolySubB config) {
    if (baseObj == null) {
      return;
    }

    // Parse fields from parent class (TestPolyBase)
    TestPolyBaseParser.config(baseObj, config);

    // Parse fieldB
    if (baseObj.has("fieldB")) {
      final boolean value = baseObj.getBoolean("fieldB");
      config.setFieldB(value);
    }
  }
}