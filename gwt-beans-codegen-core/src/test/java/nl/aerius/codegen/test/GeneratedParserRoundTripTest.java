package nl.aerius.codegen.test;

import org.junit.jupiter.api.Test;

import nl.aerius.codegen.test.types.TestRootObjectType;

/**
 * Tests the round trip functionality using the dynamically generated parser.
 * This test verifies that our parser generator produces code that correctly
 * handles all fields and types. If this test fails but
 * ExpectedParserRoundTripTest passes, it means our generator is not producing
 * correct code.
 */
class GeneratedParserRoundTripTest extends AbstractRoundTripTest {
  @Override
  protected void prepareParser() throws Exception {
    generateParser(TestRootObjectType.class);
  }

  @Test
  void shouldRoundTripBasicTypes() throws Exception {
    // First generate the parser
    prepareParser();

    // Find the appropriate parser for TestRootObjectType
    Class<?> parserClass = findParserForType(TestRootObjectType.class);

    // Create test object with all fields set
    TestRootObjectType original = TestRootObjectType.createFullObject();

    assertRoundTrip(original, parserClass);
  }

  @Test
  void shouldHandleNullValues() throws Exception {
    // First generate the parser
    prepareParser();

    // Find the appropriate parser for TestRootObjectType
    Class<?> parserClass = findParserForType(TestRootObjectType.class);

    // Create object with null values
    TestRootObjectType original = TestRootObjectType.createNullObject();

    assertRoundTrip(original, parserClass);
  }
}