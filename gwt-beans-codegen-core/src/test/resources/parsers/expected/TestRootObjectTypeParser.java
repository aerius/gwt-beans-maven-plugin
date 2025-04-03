package nl.aerius.codegen.test.generated;


import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.custom.TestCustomParserTypeParser;
import nl.aerius.codegen.test.types.ConcreteType;
import nl.aerius.codegen.test.types.TestAdvancedMapType;
import nl.aerius.codegen.test.types.TestComplexCollectionType;
import nl.aerius.codegen.test.types.TestCustomParserType;
import nl.aerius.codegen.test.types.TestEnumListType;
import nl.aerius.codegen.test.types.TestEnumType;
import nl.aerius.codegen.test.types.TestNestedMapType;
import nl.aerius.codegen.test.types.TestRootObjectType;
import nl.aerius.codegen.test.types.TestSimpleTypesType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestRootObjectTypeParser {
  public static TestRootObjectType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestRootObjectType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final TestRootObjectType config = new TestRootObjectType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestRootObjectType config) {
    if (baseObj == null) {
      return;
    }

    // Parse foo
    if (baseObj.has("foo") && !baseObj.isNull("foo")) {
      config.setFoo(baseObj.getString("foo"));
    }

    // Parse count
    if (baseObj.has("count")) {
      config.setCount(baseObj.getInteger("count"));
    }

    // Parse active
    if (baseObj.has("active")) {
      config.setActive(baseObj.getBoolean("active"));
    }

    // Parse simpleCollection
    if (baseObj.has("simpleCollection") && !baseObj.isNull("simpleCollection")) {
      config.setSimpleCollection(TestSimpleCollectionTypeParser.parse(baseObj.getObject("simpleCollection")));
    }

    // Parse simpleTypes
    if (baseObj.has("simpleTypes") && !baseObj.isNull("simpleTypes")) {
      final TestSimpleTypesType value = TestSimpleTypesTypeParser.parse(baseObj.getObject("simpleTypes"));
      config.setSimpleTypes(value);
    }

    // Parse customParserType
    if (baseObj.has("customParserType") && !baseObj.isNull("customParserType")) {
      final TestCustomParserType value = TestCustomParserTypeParser.parse(baseObj.getObject("customParserType"));
      config.setCustomParserType(value);
    }

    // Parse enumType
    if (baseObj.has("enumType") && !baseObj.isNull("enumType")) {
      final TestEnumType value = TestEnumTypeParser.parse(baseObj.getObject("enumType"));
      config.setEnumType(value);
    }

    // Parse complexCollection
    if (baseObj.has("complexCollection") && !baseObj.isNull("complexCollection")) {
      final TestComplexCollectionType value = TestComplexCollectionTypeParser.parse(baseObj.getObject("complexCollection"));
      config.setComplexCollection(value);
    }

    // Parse advancedMap
    if (baseObj.has("advancedMap") && !baseObj.isNull("advancedMap")) {
      final TestAdvancedMapType value = TestAdvancedMapTypeParser.parse(baseObj.getObject("advancedMap"));
      config.setAdvancedMap(value);
    }

    // Parse enumListType
    if (baseObj.has("enumListType") && !baseObj.isNull("enumListType")) {
      final TestEnumListType value = TestEnumListTypeParser.parse(baseObj.getObject("enumListType"));
      config.setEnumListType(value);
    }

    // Parse concreteType
    if (baseObj.has("concreteType") && !baseObj.isNull("concreteType")) {
      final ConcreteType value = ConcreteTypeParser.parse(baseObj.getObject("concreteType"));
      config.setConcreteType(value);
    }

    // Parse nestedMapType
    if (baseObj.has("nestedMapType") && !baseObj.isNull("nestedMapType")) {
      final TestNestedMapType value = TestNestedMapTypeParser.parse(baseObj.getObject("nestedMapType"));
      config.setNestedMapType(value);
    }
  }
}
