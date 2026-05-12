package nl.aerius.codegen.test.types;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Immutable test type with a {@code @JsonIgnore} field that is NOT a constructor
 * parameter (it is derived from another field). Verifies that the constructor-based
 * code path skips {@code @JsonIgnore} fields in {@code getParseableFields}, so the
 * single-arg constructor matches the single parseable field {@code name} instead of
 * failing because the analyzer thinks there are two parseable fields.
 *
 * The {@code derivedHash} getter is annotated so Jackson also leaves it out of the
 * round-trip JSON.
 */
public class TestConstructorWithIgnoredFieldType {
  private final String name;

  @JsonIgnore
  private final int derivedHash;

  public TestConstructorWithIgnoredFieldType(final String name) {
    this.name = name;
    this.derivedHash = name == null ? 0 : name.hashCode();
  }

  public String getName() {
    return name;
  }

  @JsonIgnore
  public int getDerivedHash() {
    return derivedHash;
  }

  public static TestConstructorWithIgnoredFieldType createFullObject() {
    return new TestConstructorWithIgnoredFieldType("ignored-field-test");
  }

  public static TestConstructorWithIgnoredFieldType createNullObject() {
    return new TestConstructorWithIgnoredFieldType(null);
  }
}
