package nl.overheid.aerius.codegen.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import nl.overheid.aerius.codegen.ParserGenerator;

public abstract class ParserGeneratorTestBase {
  protected static final String TEST_PACKAGE = "nl.overheid.aerius.codegen.test.generated";
  protected static final String TEST_TIMESTAMP = "2024-01-01T00:00:00";
  protected Path outputDir;
  protected Path expectedDir;

  @BeforeEach
  void setUp() throws IOException {
    // Use the test resources directory for output
    outputDir = Path.of("src/test/resources/parsers/generated");
    expectedDir = Path.of("src/test/resources/parsers/expected");

    System.out.println("\n=== Test Setup ===");
    System.out.println("Output directory: " + outputDir.toAbsolutePath());
    System.out.println("Expected directory: " + expectedDir.toAbsolutePath());

    // Clean up any existing generated files
    if (Files.exists(outputDir)) {
      System.out.println("Cleaning up existing files in output directory...");
      cleanDirectory(outputDir);
    }

    System.out.println("Creating output directory: " + outputDir.toAbsolutePath());
    Files.createDirectories(outputDir);
  }

  private void cleanDirectory(Path directory) throws IOException {
    if (Files.exists(directory)) {
      Files.walk(directory)
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> {
            try {
              Files.delete(path);
            } catch (IOException e) {
              throw new RuntimeException("Failed to delete file: " + path, e);
            }
          });
    }
  }

  protected void generateParser(final Class<?> rootClass) throws IOException {
    ParserGenerator.generateParsersForClass(rootClass, TEST_PACKAGE, outputDir.toString(), TEST_TIMESTAMP);
  }

  /**
   * Generates a parser for the given root class, using the specified custom
   * parser directory.
   * 
   * @param rootClass       The root class to generate a parser for
   * @param customParserDir The directory containing custom parsers
   * @throws IOException If an I/O error occurs
   */
  protected void generateParser(final Class<?> rootClass, final String customParserDir) throws IOException {
    ParserGenerator.generateParsersForClass(rootClass, TEST_PACKAGE, outputDir.toString(), customParserDir,
        TEST_TIMESTAMP);
  }

  /**
   * Gets the directory containing custom parsers.
   * 
   * @return The custom parser directory
   */
  protected Path getCustomParserDir() {
    return Path.of("src/test/java/nl/overheid/aerius/codegen/test/custom");
  }

  protected String getGeneratedParserContent(final String className) throws IOException {
    final Path parserPath = outputDir.resolve(className + "Parser.java");
    System.out.println("Looking for generated parser at: " + parserPath.toAbsolutePath());
    Assertions.assertTrue(Files.exists(parserPath), "Parser file should exist at: " + parserPath);
    return Files.readString(parserPath);
  }

  protected void assertGeneratedParserMatches(final String className, final String expectedContent) throws IOException {
    final String actualContent = getGeneratedParserContent(className);
    final String normalizedActual = normalizeContent(actualContent);
    final String normalizedExpected = normalizeContent(expectedContent);

    if (!normalizedExpected.equals(normalizedActual)) {
      // Find the first point of difference
      int diffIndex = findFirstDifferenceIndex(normalizedExpected, normalizedActual);

      // Extract 3 lines before and after the difference
      String[] expectedLines = normalizedExpected.split("\n");
      String[] actualLines = normalizedActual.split("\n");

      // Find the line number where the difference occurs
      int diffLine = 0;
      int currentPos = 0;
      for (String line : expectedLines) {
        if (currentPos + line.length() >= diffIndex) {
          break;
        }
        currentPos += line.length() + 1; // +1 for the newline
        diffLine++;
      }

      // Get context lines
      int startLine = Math.max(0, diffLine - 3);
      int endLine = Math.min(Math.min(expectedLines.length, actualLines.length), diffLine + 4);

      StringBuilder expectedContext = new StringBuilder();
      StringBuilder actualContext = new StringBuilder();

      for (int i = startLine; i < endLine; i++) {
        if (i == diffLine) {
          expectedContext.append(">>> "); // Highlight the difference line
          actualContext.append(">>> ");
        } else {
          expectedContext.append("    ");
          actualContext.append("    ");
        }
        expectedContext.append(i < expectedLines.length ? expectedLines[i] : "").append("\n");
        actualContext.append(i < actualLines.length ? actualLines[i] : "").append("\n");
      }

      // Create a detailed error message
      String errorMessage = String.format(
          "Generated parser content doesn't match expected for %s.\n" +
              "Difference at line %d:\n\n" +
              "Expected:\n%s\n" +
              "Actual:\n%s\n",
          className, diffLine + 1, expectedContext, actualContext);

      Assertions.fail(errorMessage);
    }
  }

  /**
   * Finds the index of the first character that differs between two strings.
   * 
   * @param expected The expected string
   * @param actual   The actual string
   * @return The index of the first difference, or -1 if the strings are identical
   */
  private int findFirstDifferenceIndex(String expected, String actual) {
    int minLength = Math.min(expected.length(), actual.length());

    for (int i = 0; i < minLength; i++) {
      if (expected.charAt(i) != actual.charAt(i)) {
        return i;
      }
    }

    // If we get here, one string might be a prefix of the other
    if (expected.length() != actual.length()) {
      return minLength;
    }

    // Strings are identical
    return -1;
  }

  protected String getExpectedParserContent(final String className) throws IOException {
    final Path parserPath = expectedDir.resolve(className + "Parser.java");
    System.out.println("Looking for expected parser at: " + parserPath.toAbsolutePath());
    Assertions.assertTrue(Files.exists(parserPath), "Expected parser file should exist at: " + parserPath);
    return Files.readString(parserPath);
  }

  private String normalizeContent(String content) {
    String result = content;

    // Remove all indentation (spaces/tabs at start of lines)
    result = result.replaceAll("(?m)^[ \\t]+", "");

    // Remove all empty lines (lines with only whitespace)
    result = result.replaceAll("(?m)^\\s*$\\n", "");

    // Normalize whitespace within lines (collapse multiple spaces to single space)
    result = result.replaceAll("[ \\t]+", " ");

    // Trim trailing whitespace at the end of each line
    result = result.replaceAll("(?m)[ \\t]+$", "");

    // Remove @Generated annotation completely since it contains a timestamp
    result = result.replaceAll("@Generated[^;]+;", "");

    // Ensure consistent line endings and remove trailing newlines
    result = result.replaceAll("\r\n", "\n");
    result = result.trim();

    // Sort imports within each group
    String[] lines = result.split("\n");
    StringBuilder sb = new StringBuilder();
    List<String> javaUtilImports = new ArrayList<>();
    List<String> javaxImports = new ArrayList<>();
    List<String> nlImports = new ArrayList<>();
    boolean inImports = false;

    for (String line : lines) {
      if (line.startsWith("import ")) {
        inImports = true;
        if (line.startsWith("import java.util.")) {
          javaUtilImports.add(line);
        } else if (line.startsWith("import javax.")) {
          javaxImports.add(line);
        } else if (line.startsWith("import nl.")) {
          nlImports.add(line);
        } else {
          sb.append(line).append("\n");
        }
      } else {
        if (inImports) {
          // Add sorted imports
          Collections.sort(javaUtilImports);
          Collections.sort(javaxImports);
          Collections.sort(nlImports);

          if (!javaUtilImports.isEmpty()) {
            javaUtilImports.forEach(imp -> sb.append(imp).append("\n"));
            sb.append("\n");
          }
          if (!javaxImports.isEmpty()) {
            javaxImports.forEach(imp -> sb.append(imp).append("\n"));
            sb.append("\n");
          }
          if (!nlImports.isEmpty()) {
            nlImports.forEach(imp -> sb.append(imp).append("\n"));
            sb.append("\n");
          }
          inImports = false;
        }
        sb.append(line).append("\n");
      }
    }

    return sb.toString().trim();
  }
}