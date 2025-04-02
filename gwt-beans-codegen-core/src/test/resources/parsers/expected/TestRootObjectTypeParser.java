package nl.aerius.codegen.test.generated;


import javax.annotation.processing.Generated;

import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestRootObjectType;
import nl.aerius.codegen.test.custom.TestCustomParserTypeParser;
import nl.aerius.codegen.test.types.ConcreteType;
import nl.aerius.codegen.test.types.TestAdvancedMapType;
import nl.aerius.codegen.test.types.TestEnumListType;
import nl.aerius.codegen.test.types.TestNestedMapType;

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
    parse(obj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle obj, final TestRootObjectType config) {
    if (obj == null) {
      return;
    }

    // Parse foo
    if (obj.has("foo") && !obj.isNull("foo")) {
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
    if (obj.has("simpleCollection") && !obj.isNull("simpleCollection")) {
      config.setSimpleCollection(TestSimpleCollectionTypeParser.parse(obj.getObject("simpleCollection")));
    }

    // Parse simpleTypes
    if (obj.has("simpleTypes") && !obj.isNull("simpleTypes")) {
      config.setSimpleTypes(TestSimpleTypesTypeParser.parse(obj.getObject("simpleTypes")));
    }

    // Parse customParserType
    if (obj.has("customParserType") && !obj.isNull("customParserType")) {
      config.setCustomParserType(TestCustomParserTypeParser.parse(obj.getObject("customParserType")));
    }

    // Parse enumType
    if (obj.has("enumType") && !obj.isNull("enumType")) {
      config.setEnumType(TestEnumTypeParser.parse(obj.getObject("enumType")));
    }

    // Parse complexCollection
    if (obj.has("complexCollection") && !obj.isNull("complexCollection")) {
      config.setComplexCollection(TestComplexCollectionTypeParser.parse(obj.getObject("complexCollection")));
    }

    // Parse advancedMap
    if (obj.has("advancedMap") && !obj.isNull("advancedMap")) {
      final TestAdvancedMapType level1Value = TestAdvancedMapTypeParser.parse(obj.getObject("advancedMap"));
      config.setAdvancedMap(level1Value);
    }

    // Parse enumListType
    if (obj.has("enumListType") && !obj.isNull("enumListType")) {
      final TestEnumListType level1Value = TestEnumListTypeParser.parse(obj.getObject("enumListType"));
      config.setEnumListType(level1Value);
    }

    // Parse concreteType
    if (obj.has("concreteType") && !obj.isNull("concreteType")) {
      final ConcreteType level1Value = ConcreteTypeParser.parse(obj.getObject("concreteType"));
      config.setConcreteType(level1Value);
    }

    // Parse nestedMapType
    if (obj.has("nestedMapType") && !obj.isNull("nestedMapType")) {
      final TestNestedMapType level1Value = TestNestedMapTypeParser.parse(obj.getObject("nestedMapType"));
      config.setNestedMapType(level1Value);
    }
  }
}
