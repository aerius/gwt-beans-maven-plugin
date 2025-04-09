package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestSimpleTypesType;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestSimpleTypesTypeParser {
  public static TestSimpleTypesType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestSimpleTypesType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final TestSimpleTypesType config = new TestSimpleTypesType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestSimpleTypesType config) {
    if (baseObj == null || config == null) {
      return;
    }

    // Parse primitiveByte
    if (baseObj.has("primitiveByte")) {
      final byte value = (byte) baseObj.getInteger("primitiveByte");
      config.setPrimitiveByte(value);
    }

    // Parse primitiveShort
    if (baseObj.has("primitiveShort")) {
      final short value = (short) baseObj.getInteger("primitiveShort");
      config.setPrimitiveShort(value);
    }

    // Parse primitiveFloat
    if (baseObj.has("primitiveFloat")) {
      final float value = baseObj.getNumber("primitiveFloat").floatValue();
      config.setPrimitiveFloat(value);
    }

    // Parse primitiveChar
    if (baseObj.has("primitiveChar")) {
      final String str = baseObj.getString("primitiveChar");
      final char value = (str != null && !str.isEmpty()) ? str.charAt(0) : 0;
      config.setPrimitiveChar(value);
    }

    // Parse primitiveLong
    if (baseObj.has("primitiveLong")) {
      final long value = baseObj.getLong("primitiveLong");
      config.setPrimitiveLong(value);
    }

    // Parse wrapperByte
    if (baseObj.has("wrapperByte") && !baseObj.isNull("wrapperByte")) {
      final Byte value = (byte) baseObj.getInteger("wrapperByte");
      config.setWrapperByte(value);
    }

    // Parse wrapperShort
    if (baseObj.has("wrapperShort") && !baseObj.isNull("wrapperShort")) {
      final Short value = (short) baseObj.getInteger("wrapperShort");
      config.setWrapperShort(value);
    }

    // Parse wrapperFloat
    if (baseObj.has("wrapperFloat") && !baseObj.isNull("wrapperFloat")) {
      final Float value = baseObj.getNumber("wrapperFloat").floatValue();
      config.setWrapperFloat(value);
    }

    // Parse wrapperChar
    if (baseObj.has("wrapperChar") && !baseObj.isNull("wrapperChar")) {
      final String str = baseObj.getString("wrapperChar");
      final Character value = (str != null && !str.isEmpty()) ? str.charAt(0) : null;
      config.setWrapperChar(value);
    }

    // Parse wrapperLong
    if (baseObj.has("wrapperLong") && !baseObj.isNull("wrapperLong")) {
      final Long value = baseObj.getLong("wrapperLong");
      config.setWrapperLong(value);
    }
  }
}
