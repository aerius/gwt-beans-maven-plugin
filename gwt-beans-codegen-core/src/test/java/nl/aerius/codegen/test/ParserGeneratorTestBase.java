package nl.aerius.codegen.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import nl.aerius.codegen.ParserGenerator;

public abstract class ParserGeneratorTestBase {
  protected static final String TEST_PACKAGE = "nl.aerius.codegen.test.generated";
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

  protected void replaceJsonObjectHandleImports() throws IOException {
    if (Files.exists(outputDir)) {
      Files.walk(outputDir)
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> {
            try {
              String content = Files.readString(path);
              content = content.replace(
                  "import nl.aerius.wui.service.json.JSONObjectHandle;",
                  "import nl.aerius.wui.service.json.JSONObjectHandle;");
              Files.writeString(path, content);
            } catch (IOException e) {
              throw new RuntimeException("Failed to process file: " + path, e);
            }
          });
    }
  }

  /**
   * Gets the directory containing custom parsers.
   * 
   * @return The custom parser directory
   */
  protected Path getCustomParserDir() {
    return Path.of("src/test/java/nl/aerius/codegen/test/custom");
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
      // Split into lines for comparison
      List<String> expectedLines = List.of(normalizedExpected.split("\n"));
      List<String> actualLines = List.of(normalizedActual.split("\n"));

      // Compare from the bottom up
      int expectedSize = expectedLines.size();
      int actualSize = actualLines.size();
      int minSize = Math.min(expectedSize, actualSize);

      // Find first difference from bottom
      int diffFromBottom = -1;
      for (int i = 1; i <= minSize; i++) {
        String expected = expectedLines.get(expectedSize - i);
        String actual = actualLines.get(actualSize - i);
        if (!expected.equals(actual)) {
          diffFromBottom = i;
          break;
        }
      }

      // Build error message
      StringBuilder message = new StringBuilder();
      message.append(String.format("Generated parser content doesn't match expected for %s.\n", className));

      if (diffFromBottom == -1) {
        message.append(String.format("Files have different lengths. Expected %d lines but got %d lines.\n",
            expectedSize, actualSize));
      } else {
        message.append(String.format("First difference found at line %d from bottom:\n\n", diffFromBottom));

        // Calculate the lines to show
        int diffLineExpected = expectedSize - diffFromBottom;
        int diffLineActual = actualSize - diffFromBottom;

        // Show 3 lines before and after the difference
        int contextBefore = 3;
        int contextAfter = 3;

        message.append("Expected content:\n");
        message.append("---------------\n");
        for (int i = Math.max(0, diffLineExpected - contextBefore); i <= Math.min(expectedSize - 1,
            diffLineExpected + contextAfter); i++) {
          String lineNum = String.format("%4d", i + 1);
          String marker = (i == diffLineExpected) ? " >>> " : "     ";
          message.append(lineNum).append(marker).append(expectedLines.get(i)).append('\n');
        }

        message.append("\nActual content:\n");
        message.append("-------------\n");
        for (int i = Math.max(0, diffLineActual - contextBefore); i <= Math.min(actualSize - 1,
            diffLineActual + contextAfter); i++) {
          String lineNum = String.format("%4d", i + 1);
          String marker = (i == diffLineActual) ? " >>> " : "     ";
          message.append(lineNum).append(marker).append(actualLines.get(i)).append('\n');
        }
      }

      Assertions.fail(message.toString());
    }
  }

  protected String getExpectedParserContent(final String className) throws IOException {
    final Path parserPath = expectedDir.resolve(className + "Parser.java");
    System.out.println("Looking for expected parser at: " + parserPath.toAbsolutePath());
    Assertions.assertTrue(Files.exists(parserPath), "Expected parser file should exist at: " + parserPath);
    return Files.readString(parserPath);
  }

  private String normalizeContent(String content) {
    List<String> lines = new ArrayList<>();
    List<String> imports = new ArrayList<>();

    // Split content into lines, normalize line endings
    String[] rawLines = content.replaceAll("\r\n", "\n").split("\n");

    // Process each line
    boolean inGeneratedAnnotation = false;
    for (String line : rawLines) {
      // Trim the line
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }

      // Handle @Generated annotation blocks
      if (line.contains("@Generated")) {
        inGeneratedAnnotation = true;
        // If it's a single-line annotation (contains both @Generated and closing
        // parenthesis)
        if (line.contains(")")) {
          inGeneratedAnnotation = false;
        }
        continue;
      }
      if (inGeneratedAnnotation) {
        if (line.contains(")")) {
          inGeneratedAnnotation = false;
        }
        continue;
      }

      // Collect imports separately
      if (line.startsWith("import ")) {
        imports.add(line);
      } else {
        // For non-imports, add any collected imports first (sorted)
        if (!imports.isEmpty()) {
          Collections.sort(imports);
          lines.addAll(imports);
          imports.clear();
        }
        lines.add(line);
      }
    }

    // Add any remaining imports at the end
    if (!imports.isEmpty()) {
      Collections.sort(imports);
      lines.addAll(imports);
    }

    // Join lines back together
    return String.join("\n", lines);
  }
}