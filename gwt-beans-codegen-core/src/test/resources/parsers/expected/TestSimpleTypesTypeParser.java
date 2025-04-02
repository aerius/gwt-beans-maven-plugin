package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import nl.aerius.wui.service.json.JSONObjectHandle;
import nl.aerius.codegen.test.types.TestSimpleTypesType;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestSimpleTypesTypeParser {
  public static TestSimpleTypesType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestSimpleTypesType parse(final JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestSimpleTypesType config = new TestSimpleTypesType();
    parse(obj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle obj, final TestSimpleTypesType config) {
    if (obj == null) {
      return;
    }

    // Parse primitiveByte
    if (obj.has("primitiveByte")) {
      final byte value = (byte) obj.getInteger("primitiveByte");
      config.setPrimitiveByte(value);
    }

    // Parse primitiveShort
    if (obj.has("primitiveShort")) {
      final short value = (short) obj.getInteger("primitiveShort");
      config.setPrimitiveShort(value);
    }

    // Parse primitiveFloat
    if (obj.has("primitiveFloat")) {
      final float value = obj.getNumber("primitiveFloat").floatValue();
      config.setPrimitiveFloat(value);
    }

    // Parse primitiveChar
    if (obj.has("primitiveChar")) {
      final String str = obj.getString("primitiveChar");
      final char value = (str != null && !str.isEmpty()) ? str.charAt(0) : 0;
      config.setPrimitiveChar(value);
    }

    // Parse primitiveLong
    if (obj.has("primitiveLong")) {
      final long value = obj.getLong("primitiveLong");
      config.setPrimitiveLong(value);
    }

    // Parse wrapperByte
    if (obj.has("wrapperByte") && !obj.isNull("wrapperByte")) {
      final Byte value = (byte) obj.getInteger("wrapperByte");
      config.setWrapperByte(value);
    }

    // Parse wrapperShort
    if (obj.has("wrapperShort") && !obj.isNull("wrapperShort")) {
      final Short value = (short) obj.getInteger("wrapperShort");
      config.setWrapperShort(value);
    }

    // Parse wrapperFloat
    if (obj.has("wrapperFloat") && !obj.isNull("wrapperFloat")) {
      final Float value = obj.getNumber("wrapperFloat").floatValue();
      config.setWrapperFloat(value);
    }

    // Parse wrapperChar
    if (obj.has("wrapperChar") && !obj.isNull("wrapperChar")) {
      final String str = obj.getString("wrapperChar");
      final Character value = (str != null && !str.isEmpty()) ? str.charAt(0) : null;
      config.setWrapperChar(value);
    }

    // Parse wrapperLong
    if (obj.has("wrapperLong") && !obj.isNull("wrapperLong")) {
      final Long value = obj.getLong("wrapperLong");
      config.setWrapperLong(value);
    }
  }
}
