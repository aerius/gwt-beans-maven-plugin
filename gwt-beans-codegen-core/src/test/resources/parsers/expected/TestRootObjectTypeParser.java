package nl.overheid.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;
import nl.overheid.aerius.codegen.test.json.JSONObjectHandle;
import nl.overheid.aerius.codegen.test.types.TestRootObjectType;
import nl.overheid.aerius.codegen.test.custom.TestCustomParserTypeParser;

@Generated(value = "nl.overheid.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestRootObjectTypeParser {
  public static TestRootObjectType parse(String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestRootObjectType parse(JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestRootObjectType config = new TestRootObjectType();

    // Parse foo
    if (obj.has("foo")) {
      config.setFoo(obj.getString("foo"));
    }

    // Parse count
    if (obj.has("count")) {
      config.setCount(obj.getInteger("count"));
    }

    // Parse active
    if (obj.has("active")) {
      config.setActive(obj.getBoolean("active"));
    }

    // Parse simpleCollection
    if (obj.has("simpleCollection")) {
      config.setSimpleCollection(TestSimpleCollectionTypeParser.parse(obj.getObject("simpleCollection")));
    }

    // Parse simpleTypes
    if (obj.has("simpleTypes")) {
      config.setSimpleTypes(TestSimpleTypesTypeParser.parse(obj.getObject("simpleTypes")));
    }

    // Parse customParserType
    if (obj.has("customParserType")) {
      config.setCustomParserType(TestCustomParserTypeParser.parse(obj.getObject("customParserType")));
    }

    // Parse enumType
    if (obj.has("enumType")) {
      config.setEnumType(TestEnumTypeParser.parse(obj.getObject("enumType")));
    }

    // Parse complexCollection
    if (obj.has("complexCollection")) {
      config.setComplexCollection(TestComplexCollectionTypeParser.parse(obj.getObject("complexCollection")));
    }

    return config;
  }
}
