package nl.overheid.aerius.codegen.test;

import org.junit.jupiter.api.Test;

import nl.overheid.aerius.codegen.test.types.TestComplexCollectionType;

class ComplexCollectionTest extends AbstractRoundTripTest {

  @Override
  protected void prepareParser() throws Exception {
    generateParser(TestComplexCollectionType.class);
  }

  @Test
  void shouldHandleMapWithCustomObjectValues() throws Exception {
    // First generate the parser
    prepareParser();

    // Find the appropriate parser for TestComplexCollectionType
    Class<?> parserClass = findParserForType(TestComplexCollectionType.class);

    // Create test object with all fields populated
    TestComplexCollectionType original = TestComplexCollectionType.createFullObject();

    // Test round trip
    assertRoundTrip(original, parserClass);
  }

  @Test
  void shouldHandleNullValues() throws Exception {
    // First generate the parser
    prepareParser();

    // Find the appropriate parser for TestComplexCollectionType
    Class<?> parserClass = findParserForType(TestComplexCollectionType.class);

    // Create test object with null values
    TestComplexCollectionType original = TestComplexCollectionType.createNullObject();

    // Test round trip
    assertRoundTrip(original, parserClass);
  }
}