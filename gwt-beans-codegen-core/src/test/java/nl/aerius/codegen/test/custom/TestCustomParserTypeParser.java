package nl.aerius.codegen.test.custom;

import nl.aerius.codegen.test.types.TestCustomParserType;
import nl.aerius.json.JSONObjectHandle;

/**
 * Custom parser for TestCustomParserType.
 * This parser includes side effects tracking for testing purposes.
 * The parse count is incremented each time the parse method is called,
 * which allows tests to verify that this custom parser was used.
 */
public class TestCustomParserTypeParser {
  /**
   * Counter for tracking how many times the parse method has been called.
   * Used for testing to verify that the custom parser is being used.
   */
  private static int parseCount = 0;

  /**
   * Resets the parse count to zero.
   * Should be called before each test to ensure accurate tracking.
   */
  public static void resetParseCount() {
    parseCount = 0;
  }

  /**
   * Gets the current parse count.
   * 
   * @return The number of times the parse method has been called
   */
  public static int getParseCount() {
    return parseCount;
  }

  /**
   * Parses a JSON object into a TestCustomParserType.
   * Increments the parse count for testing verification.
   * 
   * @param json The JSON object to parse
   * @return A new TestCustomParserType instance
   */
  public static TestCustomParserType parse(final JSONObjectHandle json) {
    // Increment the parse count for testing verification
    parseCount++;

    // Handle null JSON
    if (json == null) {
      return null;
    }

    final TestCustomParserType config = new TestCustomParserType();

    if (json.has("customField")) {
      config.setCustomField(json.getString("customField"));
    }

    if (json.has("customValue")) {
      config.setCustomValue(json.getInteger("customValue"));
    }

    return config;
  }

  /**
   * Parses a JSON string into a TestCustomParserType.
   * 
   * @param jsonString The JSON string to parse
   * @return A new TestCustomParserType instance
   */
  public static TestCustomParserType parse(final String jsonString) {
    return parse(JSONObjectHandle.fromText(jsonString));
  }
}