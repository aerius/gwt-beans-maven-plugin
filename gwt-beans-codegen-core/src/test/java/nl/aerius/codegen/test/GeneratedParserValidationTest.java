package nl.aerius.codegen.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

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
@TestInstance(Lifecycle.PER_CLASS) // Use PER_CLASS lifecycle for @TestFactory setup
class GeneratedParserValidationTest extends ParserGeneratorTestBase {

  // Generate parsers once before tests run
  @org.junit.jupiter.api.BeforeAll
  void generateAllParsers() throws IOException {
    System.out.println("Generating parsers before test factory execution...");
    generateParser(TestRootObjectType.class, getCustomParserDir().toString());
      System.out.println("Parser generation complete.");
  }

  @TestFactory
  Stream<DynamicTest> shouldGenerateMatchingParsers() throws IOException {
    System.out.println("Creating dynamic tests for parser comparison...");

    // Get all expected parser files
    List<Path> expectedParserFiles = Files.list(expectedDir)
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().endsWith("Parser.java"))
        .sorted() // Sort for consistent test order
        .collect(Collectors.toList());

    // Verify that generated directory exists and is not empty (basic check)
    Assertions.assertTrue(Files.exists(outputDir) && Files.list(outputDir).findAny().isPresent(),
        "Generated parser directory should exist and contain files after generation.");

    // Create a dynamic test for each expected parser file
    return expectedParserFiles.stream().map(expectedFile -> {
      String fileName = expectedFile.getFileName().toString();
      String className = fileName.replace("Parser.java", "");

      return DynamicTest.dynamicTest("Compare parser: " + className, () -> {
        System.out.println("Executing dynamic test for: " + className);
        String expectedContent = getExpectedParserContent(className);
        // Assert that the generated parser matches the expected parser
        // This call will now fail with a detailed diff (after we modify the base class)
        assertGeneratedParserMatches(className, expectedContent);
      });
    });
  }

  // Removed compareAllParsers method as logic moved to @TestFactory
}