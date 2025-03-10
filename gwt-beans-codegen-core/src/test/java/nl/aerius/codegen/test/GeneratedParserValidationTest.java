package nl.aerius.codegen.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.aerius.codegen.test.types.TestRootObjectType;

/**
 * Tests that the generated parser matches the expected parser exactly in terms
 * of code.
 * This validates that our parser generator produces the same output as our
 * reference
 * implementation. If this test fails, it means our generator is producing code
 * that
 * doesn't match our expected format.
 */
class GeneratedParserValidationTest extends ParserGeneratorTestBase {
  @Test
  void shouldGenerateMatchingParser() throws IOException {
    // When
    generateParser(TestRootObjectType.class, getCustomParserDir().toString());

    // Then
    compareAllParsers();
  }

  /**
   * Compares all parser files in the expected and generated directories.
   * This ensures that all generated parsers match their expected versions.
   */
  private void compareAllParsers() throws IOException {
    // Get all expected parser files
    List<Path> expectedParserFiles = Files.list(expectedDir)
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().endsWith("Parser.java"))
        .collect(Collectors.toList());

    // Verify that all expected files exist in the generated directory
    for (Path expectedFile : expectedParserFiles) {
      String fileName = expectedFile.getFileName().toString();
      String className = fileName.replace("Parser.java", "");

      System.out.println("Comparing parser: " + className);

      // Get the expected content
      String expectedContent = getExpectedParserContent(className);

      // Assert that the generated parser matches the expected parser
      assertGeneratedParserMatches(className, expectedContent);
    }

    // Verify that no extra files were generated
    List<Path> generatedParserFiles = Files.list(outputDir)
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().endsWith("Parser.java"))
        .collect(Collectors.toList());

    Assertions.assertEquals(
        expectedParserFiles.size(),
        generatedParserFiles.size(),
        "Number of generated parser files should match number of expected parser files");
  }
}