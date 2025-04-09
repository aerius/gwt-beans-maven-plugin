package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.polymorphic.TestPolyBase;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "GENERATION_TIMESTAMP")
public class TestPolyBaseParser {

  public static TestPolyBase parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }
    JSONObjectHandle handle = JSONObjectHandle.fromText(jsonText);
    return parse(handle);
  }

  public static TestPolyBase parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    if (!baseObj.has("_type") || baseObj.isNull("_type")) {
      throw new RuntimeException("Expected string for type discriminator field '_type', got different type");
    }

    String typeName = baseObj.getString("_type");
    if (typeName == null) {
      throw new RuntimeException("Type discriminator field '_type' is not a string");
    }

    switch (typeName) {
    case "TypeA":
      return TestPolySubAParser.parse(baseObj);
    case "TypeB":
      return TestPolySubBParser.parse(baseObj);
    default:
      throw new RuntimeException("Unknown type name '" + typeName + "' for TestPolyBase");
    }
  }

  public static void config(final JSONObjectHandle baseObj, final TestPolyBase instance) {
    if (baseObj == null) {
      return;
    }

    // Parse baseField
    if (baseObj.has("baseField") && !baseObj.isNull("baseField")) {
      final String value = baseObj.getString("baseField");
      instance.setBaseField(value);
    }
  }
}
