package nl.aerius.codegen.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import nl.aerius.codegen.test.types.TestRootObjectType;

/**
 * Tests the round trip functionality (JSON serialization -> parsing -> object
 * comparison) using our expected/reference parser implementation. This test
 * verifies that our reference implementation correctly handles all fields and
 * types. If this test fails, it means our reference implementation is incorrect
 * and needs to be fixed.
 */
class ExpectedParserRoundTripTest extends AbstractRoundTripTest {
  @Override
  protected void prepareParser() throws Exception {
    // Copy all parser files from expected to generated directory
    Path expectedDir = Path.of("src/test/resources/parsers/expected");
    Path generatedDir = Path.of("src/test/resources/parsers/generated");

    // Ensure the generated directory exists
    Files.createDirectories(generatedDir);

    // Copy all .java files from expected to generated
    try (Stream<Path> paths = Files.list(expectedDir)) {
      paths.filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> {
            try {
              Path targetPath = generatedDir.resolve(path.getFileName());
              Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
              System.out.println("Copied " + path + " to " + targetPath);
            } catch (IOException e) {
              throw new RuntimeException("Failed to copy parser file: " + path, e);
            }
          });
    }
  }

  @Test
  void shouldRoundTripBasicTypes() throws Exception {
    // First copy the expected parsers
    prepareParser();

    // Find the appropriate parser for TestRootObjectType
    Class<?> parserClass = findParserForType(TestRootObjectType.class);

    // Create test object with all fields set
    TestRootObjectType original = TestRootObjectType.createFullObject();

    assertRoundTrip(original, parserClass);
  }

  @Test
  void shouldHandleNullValues() throws Exception {
    // First copy the expected parsers
    prepareParser();

    // Find the appropriate parser for TestRootObjectType
    Class<?> parserClass = findParserForType(TestRootObjectType.class);

    // Create object with null values
    TestRootObjectType original = TestRootObjectType.createNullObject();

    assertRoundTrip(original, parserClass);
  }
}