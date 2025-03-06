package nl.overheid.aerius.codegen.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.overheid.aerius.codegen.test.custom.TestCustomParserTypeParser;
import nl.overheid.aerius.codegen.test.types.TestCustomParserType;
import nl.overheid.aerius.codegen.test.types.TestRootObjectType;

/**
 * Tests that custom parsers are used instead of generated ones.
 * Uses a side-effects based verification approach by tracking
 * the number of times the custom parser is called.
 */
class CustomParserTest extends AbstractRoundTripTest {

  @BeforeEach
  void resetCustomParserCount() {
    try {
      // Reset the parse count in the custom parser
      TestCustomParserTypeParser.resetParseCount();
      System.out.println("Successfully reset parse count");
    } catch (Exception e) {
      System.err.println("Failed to reset custom parser count: " + e.getMessage());
      throw new RuntimeException("Could not reset custom parser count", e);
    }
  }

  @Override
  protected void prepareParser() throws Exception {
    // Pass the custom parser directory to the generator
    String customParserDir = getCustomParserDir().toString();
    generateParser(TestRootObjectType.class, customParserDir);
  }

  @Test
  void shouldUseCustomParserInsteadOfGeneratedOne() throws Exception {
    // First generate the parsers
    prepareParser();

    // Verify that no parser was generated for the custom type
    Path generatedParserFile = outputDir.toPath().resolve("TestCustomParserTypeParser.java");
    assertFalse(Files.exists(generatedParserFile),
        "Custom parser should not be copied to generated directory");

    // Find the appropriate parser for TestRootObjectType
    Class<?> rootParserClass = findParserForType(TestRootObjectType.class);
    System.out.println("Found root parser class: " + rootParserClass.getName());

    // Create test object with all fields set
    TestRootObjectType original = TestRootObjectType.createFullObject();

    // Serialize to JSON
    String originalJson = objectMapper.writeValueAsString(original);
    System.out.println("Serialized JSON: " + originalJson);

    // Parse using our parser
    TestRootObjectType parsed = (TestRootObjectType) rootParserClass.getMethod("parse", String.class)
        .invoke(null, originalJson);

    // Verify that the custom parser was used by checking the parse count
    int parseCount = TestCustomParserTypeParser.getParseCount();
    System.out.println("Custom parser was called " + parseCount + " times");
    assertTrue(parseCount > 0,
        "Custom parser should have been called at least once");

    // Verify parsing was successful
    assertNotNull(parsed.getCustomParserType(), "Custom parser type should not be null");
    assertEquals(
        TestCustomParserType.createFullObject().getCustomField(),
        parsed.getCustomParserType().getCustomField(),
        "Custom field should be parsed correctly");
    assertEquals(
        TestCustomParserType.createFullObject().getCustomValue(),
        parsed.getCustomParserType().getCustomValue(),
        "Custom value should be parsed correctly");
  }
}