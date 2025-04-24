package nl.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestPrimitiveArrayType;
import nl.aerius.json.JSONArrayHandle;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "GENERATION_TIMESTAMP")
public class TestPrimitiveArrayTypeParser {

  public static TestPrimitiveArrayType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }
    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestPrimitiveArrayType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }
    final TestPrimitiveArrayType config = new TestPrimitiveArrayType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestPrimitiveArrayType config) {
    if (baseObj == null || config == null) {
      return;
    }

    System.out.println("baseObj: " + baseObj);

    // Parse stringArray
    if (baseObj.has("stringArray") && !baseObj.isNull("stringArray")) {
      final JSONArrayHandle array = baseObj.getArray("stringArray");
      final List<String> list = new ArrayList<>();
      array.forEachString(list::add);
      config.setStringArray(list.toArray(new String[0]));
    }

    // Parse intArray
    if (baseObj.has("intArray") && !baseObj.isNull("intArray")) {
      final JSONArrayHandle array = baseObj.getArray("intArray");
      final List<Integer> list = new ArrayList<>();
      array.forEachInteger(list::add);
      config.setIntArray(list.stream().mapToInt(v -> v).toArray());
    }

    // Parse integerArray (Wrapper type)
    if (baseObj.has("integerArray") && !baseObj.isNull("integerArray")) {
      final JSONArrayHandle array = baseObj.getArray("integerArray");
      final List<Integer> list = new ArrayList<>();
      array.forEachInteger(list::add);
      config.setIntegerArray(list.toArray(new Integer[list.size()]));
    }

    // Parse doubleArray
    if (baseObj.has("doubleArray") && !baseObj.isNull("doubleArray")) {
      final JSONArrayHandle array = baseObj.getArray("doubleArray");
      final List<Double> list = new ArrayList<>();
      array.forEachNumber(list::add);
      config.setDoubleArray(list.stream().mapToDouble(d -> d).toArray());
    }

    // Parse numberArray (Wrapper type)
    if (baseObj.has("numberArray") && !baseObj.isNull("numberArray")) {
      final JSONArrayHandle array = baseObj.getArray("numberArray");
      final List<Double> list = new ArrayList<>();
      array.forEachNumber(list::add);
      config.setNumberArray(list.toArray(new Double[list.size()]));
    }
  }
}