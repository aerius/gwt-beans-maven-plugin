package nl.aerius.codegen.test.types;

import java.util.List;

/**
 * Java record fixture: exercises the constructor-based path against a record's
 * compiler-generated canonical constructor and {@code getRecordComponents()}
 * (instead of source-file parsing). Includes a generic collection component to
 * also exercise the variableName-aware collection parser.
 */
public record TestRecordType(String name, int value, List<String> tags) {
  public static TestRecordType createFullObject() {
    return new TestRecordType("record-test", 7, List.of("alpha", "beta"));
  }

  public static TestRecordType createNullObject() {
    return new TestRecordType(null, 0, null);
  }
}
