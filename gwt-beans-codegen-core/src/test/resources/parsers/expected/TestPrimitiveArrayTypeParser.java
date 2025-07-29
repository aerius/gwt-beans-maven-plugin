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

    // Parse stringArray
    if (baseObj.has("stringArray") && !baseObj.isNull("stringArray")) {
      String[] array = null;
      final JSONArrayHandle jsonArray = baseObj.getArray("stringArray");
      if (jsonArray != null) {
        final List<String> tempList = new ArrayList<>();
        jsonArray.forEachString(tempList::add);
        array = tempList.toArray(new String[0]);
      }
      config.setStringArray(array);
    }

    // Parse intArray
    if (baseObj.has("intArray") && !baseObj.isNull("intArray")) {
      int[] array = null;
      final JSONArrayHandle jsonArray = baseObj.getArray("intArray");
      if (jsonArray != null) {
        final List<Integer> tempList = new ArrayList<>();
        jsonArray.forEachInteger(tempList::add);
        array = tempList.stream().mapToInt(i -> i != null ? i.intValue() : 0).toArray();
      }
      config.setIntArray(array);
    }

    // Parse integerArray
    if (baseObj.has("integerArray") && !baseObj.isNull("integerArray")) {
      Integer[] array = null;
      final JSONArrayHandle jsonArray = baseObj.getArray("integerArray");
      if (jsonArray != null) {
        final List<Integer> tempList = new ArrayList<>();
        jsonArray.forEachInteger(tempList::add);
        array = tempList.toArray(new Integer[0]);
      }
      config.setIntegerArray(array);
    }

    // Parse doubleArray
    if (baseObj.has("doubleArray") && !baseObj.isNull("doubleArray")) {
      double[] array = null;
      final JSONArrayHandle jsonArray = baseObj.getArray("doubleArray");
      if (jsonArray != null) {
        final List<Double> tempList = new ArrayList<>();
        jsonArray.forEachNumber(tempList::add);
        array = tempList.stream().mapToDouble(d -> d != null ? d.doubleValue() : 0.0).toArray();
      }
      config.setDoubleArray(array);
    }

    // Parse numberArray
    if (baseObj.has("numberArray") && !baseObj.isNull("numberArray")) {
      Double[] array = null;
      final JSONArrayHandle jsonArray = baseObj.getArray("numberArray");
      if (jsonArray != null) {
        final List<Double> tempList = new ArrayList<>();
        jsonArray.forEachNumber(tempList::add);
        array = tempList.toArray(new Double[0]);
      }
      config.setNumberArray(array);
    }
  }
}
