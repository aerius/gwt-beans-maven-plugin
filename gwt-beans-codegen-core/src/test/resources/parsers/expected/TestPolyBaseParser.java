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

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestPolyBase parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    if (!baseObj.has("_type") || baseObj.isNull("_type")) {
      throw new RuntimeException("Expected string for type discriminator field '_type', got different type");
    }

    final String typeName = baseObj.getString("_type");
    switch (typeName) {
    case "TypeA":
      return TestPolySubAParser.parse(baseObj);
    case "TypeB":
      return TestPolySubBParser.parse(baseObj);
    default:
      throw new RuntimeException("Unknown type name '" + typeName + "' for TestPolyBase");
    }
  }

  public static void parse(final JSONObjectHandle baseObj, final TestPolyBase config) {
    if (baseObj == null || config == null) {
      return;
    }

    // Parse baseField
    if (baseObj.has("baseField") && !baseObj.isNull("baseField")) {
      final String value = baseObj.getString("baseField");
      config.setBaseField(value);
    }
  }
}
