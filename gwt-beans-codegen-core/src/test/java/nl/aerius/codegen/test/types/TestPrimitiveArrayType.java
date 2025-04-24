package nl.aerius.codegen.test.types;

import java.util.Arrays;
import java.util.Objects;

public class TestPrimitiveArrayType {
  private String[] stringArray;
  private int[] intArray; // Primitive int array
  private Integer[] integerArray; // Wrapper Integer array
  private double[] doubleArray; // Primitive double array
  private Double[] numberArray; // Wrapper Double array (matches JSON number)

  public String[] getStringArray() {
    return stringArray;
  }

  public void setStringArray(String[] stringArray) {
    this.stringArray = stringArray;
  }

  public int[] getIntArray() {
    return intArray;
  }

  public void setIntArray(int[] intArray) {
    this.intArray = intArray;
  }

  public Integer[] getIntegerArray() {
    return integerArray;
  }

  public void setIntegerArray(Integer[] integerArray) {
    this.integerArray = integerArray;
  }

  public double[] getDoubleArray() {
    return doubleArray;
  }

  public void setDoubleArray(double[] doubleArray) {
    this.doubleArray = doubleArray;
  }

  public Double[] getNumberArray() {
    return numberArray;
  }

  public void setNumberArray(Double[] numberArray) {
    this.numberArray = numberArray;
  }

  public static TestPrimitiveArrayType createFullObject() {
    TestPrimitiveArrayType obj = new TestPrimitiveArrayType();
    obj.setStringArray(new String[] {"hello", "world", ""});
    obj.setIntArray(new int[] {1, 2, -3});
    obj.setIntegerArray(new Integer[] {10, 5, 30});
    obj.setDoubleArray(new double[] {1.1, 2.2, -3.3});
    obj.setNumberArray(new Double[] {10.1, 2D, 30.3});
    return obj;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TestPrimitiveArrayType that = (TestPrimitiveArrayType) o;
    return Arrays.equals(stringArray, that.stringArray) &&
        Arrays.equals(intArray, that.intArray) &&
        Arrays.equals(integerArray, that.integerArray) &&
        Arrays.equals(doubleArray, that.doubleArray) &&
        Arrays.equals(numberArray, that.numberArray);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(integerArray, numberArray);
    result = 31 * result + Arrays.hashCode(stringArray);
    result = 31 * result + Arrays.hashCode(intArray);
    result = 31 * result + Arrays.hashCode(doubleArray);
    return result;
  }
}