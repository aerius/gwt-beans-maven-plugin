package nl.overheid.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.overheid.aerius.codegen.test.json.JSONObjectHandle;
import nl.overheid.aerius.codegen.test.types.TestSimpleTypesType;

@Generated(value = "nl.overheid.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestSimpleTypesTypeParser {
  public static TestSimpleTypesType parse(String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestSimpleTypesType parse(JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestSimpleTypesType config = new TestSimpleTypesType();

    // Parse primitiveByte
    if (obj.has("primitiveByte")) {
      config.setPrimitiveByte((byte) obj.getInteger("primitiveByte"));
    }

    // Parse primitiveShort
    if (obj.has("primitiveShort")) {
      config.setPrimitiveShort((short) obj.getInteger("primitiveShort"));
    }

    // Parse primitiveFloat
    if (obj.has("primitiveFloat")) {
      config.setPrimitiveFloat(obj.getNumber("primitiveFloat").floatValue());
    }

    // Parse primitiveChar
    if (obj.has("primitiveChar")) {
      String charStr = obj.getString("primitiveChar");
      if (charStr != null && !charStr.isEmpty()) {
        config.setPrimitiveChar(charStr.charAt(0));
      }
    }

    // Parse primitiveLong
    if (obj.has("primitiveLong")) {
      config.setPrimitiveLong(obj.getLong("primitiveLong"));
    }

    // Parse wrapperByte
    if (obj.has("wrapperByte") && !obj.isNull("wrapperByte")) {
      config.setWrapperByte((byte) obj.getInteger("wrapperByte"));
    }

    // Parse wrapperShort
    if (obj.has("wrapperShort") && !obj.isNull("wrapperShort")) {
      config.setWrapperShort((short) obj.getInteger("wrapperShort"));
    }

    // Parse wrapperFloat
    if (obj.has("wrapperFloat") && !obj.isNull("wrapperFloat")) {
      config.setWrapperFloat(obj.getNumber("wrapperFloat").floatValue());
    }

    // Parse wrapperChar
    if (obj.has("wrapperChar") && !obj.isNull("wrapperChar")) {
      String charStr = obj.getString("wrapperChar");
      if (charStr != null && !charStr.isEmpty()) {
        config.setWrapperChar(charStr.charAt(0));
      }
    }

    // Parse wrapperLong
    if (obj.has("wrapperLong") && !obj.isNull("wrapperLong")) {
      config.setWrapperLong(obj.getLong("wrapperLong"));
    }

    return config;
  }
}