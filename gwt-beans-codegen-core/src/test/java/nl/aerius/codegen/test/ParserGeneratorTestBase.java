package nl.aerius.codegen.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import nl.aerius.codegen.ParserGenerator;
import nl.aerius.codegen.generator.ParserWriterUtils;
import nl.aerius.codegen.util.ClassFinder;
import nl.aerius.codegen.util.Logger;

// Removed TestInstance - let subclasses handle lifecycle
public abstract class ParserGeneratorTestBase {
  protected static final String TEST_PACKAGE = "nl.aerius.codegen.test.generated";
  protected static final String TEST_TIMESTAMP = "2024-01-01T00:00:00";

  private static final ClassFinder CLASSFINDER = new ClassFinder() {};
  private static final Logger LOGGER = new Logger() {};

  // Make paths static as they are initialized once
  protected static Path outputDir;
  protected static Path expectedDir;

  // Use static @BeforeAll for one-time setup
  @BeforeAll
  static void setUpDirectories() throws IOException {
    // Use the test resources directory for output
    outputDir = Path.of("src/test/resources/parsers/generated");
    expectedDir = Path.of("src/test/resources/parsers/expected");

    System.out.println("\n=== Test Base @BeforeAll ===");
    System.out.println("Output directory: " + outputDir.toAbsolutePath());
    System.out.println("Expected directory: " + expectedDir.toAbsolutePath());

    // Clean up any existing generated files
    System.out.println("Cleaning up existing files in output directory...");
    cleanDirectory(outputDir);

    System.out.println("Creating output directory: " + outputDir.toAbsolutePath());
    Files.createDirectories(outputDir);
    System.out.println("=== Test Base @BeforeAll Complete ===");

    ParserWriterUtils.initParsers(CLASSFINDER, LOGGER);
  }

  // Helper method remains static
  private static void cleanDirectory(final Path directory) throws IOException {
    if (Files.exists(directory)) {
      // Use try-with-resources for the stream to ensure it's closed
      try (Stream<Path> walk = Files.walk(directory)) {
        walk.filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(path -> {
              try {
                Files.delete(path);
              } catch (final IOException e) {
                // Wrap in RuntimeException as forEach lambda cannot directly throw checked exceptions
                throw new RuntimeException("Failed to delete file: " + path, e);
              }
            });
      }
      // Clean empty subdirectories if needed (optional)
    }
  }

  // generateParser methods remain non-static as they might be called
  // by instance methods (@Test, @BeforeEach in subclasses if needed)
  // but they rely on static outputDir.
  protected void generateParser(final Class<?> rootClass) throws IOException {
    if (outputDir == null)
      throw new IllegalStateException("outputDir not initialized. Ensure setUpDirectories() ran.");
    // Call the most specific method, providing fixed generator name and details for testing
    ParserGenerator.generateParsersForClass(rootClass, TEST_PACKAGE, outputDir.toString(), null /* customParserDir */,
        "nl.aerius.codegen.ParserGenerator", CLASSFINDER, LOGGER);
  }

  protected void generateParser(final Class<?> rootClass, final String customParserDir) throws IOException {
    if (outputDir == null)
      throw new IllegalStateException("outputDir not initialized. Ensure setUpDirectories() ran.");
    // Call the most specific method, providing fixed generator name and details for testing
    ParserGenerator.generateParsersForClass(rootClass, TEST_PACKAGE, outputDir.toString(), customParserDir, "nl.aerius.codegen.ParserGenerator",
        CLASSFINDER, LOGGER);
  }

  // This method modifies files, so fine as non-static if called after generation
  protected void replaceJsonObjectHandleImports() throws IOException {
    if (outputDir == null || !Files.exists(outputDir))
      return;
    try (Stream<Path> walk = Files.walk(outputDir)) {
      walk.filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> {
            try {
              String content = Files.readString(path);
              // Corrected target package assuming it's nl.aerius.json
              content = content.replace(
                  "import nl.aerius.wui.service.json.JSONObjectHandle;",
                  "import nl.aerius.json.JSONObjectHandle;");
              Files.writeString(path, content);
            } catch (final IOException e) {
              throw new RuntimeException("Failed to process file: " + path, e);
            }
          });
    }
  }

  // Fine as non-static
  protected Path getCustomParserDir() {
    return Path.of("src/test/java/nl/aerius/codegen/test/custom");
  }

  // Relies on static paths, but okay as non-static methods
  protected String getGeneratedParserContent(final String className) throws IOException {
    if (outputDir == null)
      throw new IllegalStateException("outputDir not initialized.");
    final Path parserPath = outputDir.resolve(TEST_PACKAGE.replace(".", "/") + "/" + className + "Parser.java");
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
      final List<String> expectedLines = List.of(normalizedExpected.split("\n"));
      final List<String> actualLines = List.of(normalizedActual.split("\n"));

      // Compare from the bottom up
      final int expectedSize = expectedLines.size();
      final int actualSize = actualLines.size();
      final int minSize = Math.min(expectedSize, actualSize);

      // Find first difference from bottom
      int diffFromBottom = -1;
      for (int i = 1; i <= minSize; i++) {
        final String expected = expectedLines.get(expectedSize - i);
        final String actual = actualLines.get(actualSize - i);
        if (!expected.equals(actual)) {
          diffFromBottom = i;
          break;
        }
      }

      // Build error message
      final StringBuilder message = new StringBuilder();
      message.append(String.format("Generated parser content doesn't match expected for %s.\n", className));

      if (diffFromBottom == -1 && expectedSize != actualSize) {
        // Only difference is file length
        message.append(String.format("Files have different lengths. Expected %d lines but got %d lines.\n",
            expectedSize, actualSize));
      } else if (diffFromBottom != -1) {
        // Difference found within common lines
        message.append(String.format("First difference found near line %d from bottom:\n\n", diffFromBottom));

        // Calculate the lines to show
        final int diffLineExpected = expectedSize - diffFromBottom;
        final int diffLineActual = actualSize - diffFromBottom;

        // Show context lines before and after the difference
        final int contextBefore = 3;
        final int contextAfter = 3;

        message.append("Expected content:\n");
        message.append("---------------\n");
        for (int i = Math.max(0, diffLineExpected - contextBefore); i <= Math.min(expectedSize - 1, diffLineExpected + contextAfter); i++) {
          final String lineNum = String.format("%4d", i + 1); // 1-based line number
          final String marker = (i == diffLineExpected) ? " >>> " : "     ";
          message.append(lineNum).append(marker).append(expectedLines.get(i)).append('\n');
        }

        message.append("\nActual content:\n");
        message.append("-------------\n");
        for (int i = Math.max(0, diffLineActual - contextBefore); i <= Math.min(actualSize - 1, diffLineActual + contextAfter); i++) {
          final String lineNum = String.format("%4d", i + 1); // 1-based line number
          final String marker = (i == diffLineActual) ? " >>> " : "     ";
          message.append(lineNum).append(marker).append(actualLines.get(i)).append('\n');
        }
      } else {
        // This case should ideally not happen if strings are not equal but no diff found
        message.append("Content mismatch detected, but difference location could not be pinpointed precisely.\n");
        message.append("Expected Length: ").append(expectedSize).append(", Actual Length: ").append(actualSize).append("\n");
      }
      Assertions.fail(message.toString());
    }
  }

  protected String getExpectedParserContent(final String className) throws IOException {
    if (expectedDir == null)
      throw new IllegalStateException("expectedDir not initialized.");
    final Path parserPath = expectedDir.resolve(className + "Parser.java");
    System.out.println("Looking for expected parser at: " + parserPath.toAbsolutePath());
    Assertions.assertTrue(Files.exists(parserPath), "Expected parser file should exist at: " + parserPath);
    return Files.readString(parserPath);
  }

  // normalizeContent remains non-static as it operates on string input
  private String normalizeContent(final String content) {
    final List<String> lines = new ArrayList<>();
    final List<String> imports = new ArrayList<>();

    // Split content into lines, normalize line endings
    final String[] rawLines = content.replaceAll("\r\n", "\n").split("\n");

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
      }
      if (inGeneratedAnnotation) {
        if (line.endsWith(")")) {
          inGeneratedAnnotation = false;
        }
        continue;
      }

      // Collect imports separately
      if (line.startsWith("import ")) {
        // Filter out specific JSON imports to ignore differences
        if (line.startsWith("import nl.aerius.wui.service.json.") || line.startsWith("import nl.aerius.json.")) {
          continue; // Skip this import line
        }
        // Exclude the diff utils import itself from normalization comparison if needed
        // if (!line.contains("com.github.difflib")) {
        imports.add(line);
        // }
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