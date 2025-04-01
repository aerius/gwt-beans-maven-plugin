package nl.aerius.codegen.test.generated;


import javax.annotation.processing.Generated;
import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestRootObjectType;
import nl.aerius.codegen.test.custom.TestCustomParserTypeParser;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestRootObjectTypeParser {
  public static TestRootObjectType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestRootObjectType parse(final JSONObjectHandle obj) {
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

    // Parse advancedMap
    if (obj.has("advancedMap")) {
      config.setAdvancedMap(TestAdvancedMapTypeParser.parse(obj.getObject("advancedMap")));
    }

    // Parse enumListType
    if (obj.has("enumListType")) {
      config.setEnumListType(TestEnumListTypeParser.parse(obj.getObject("enumListType")));
    }

    return config;
  }
}
