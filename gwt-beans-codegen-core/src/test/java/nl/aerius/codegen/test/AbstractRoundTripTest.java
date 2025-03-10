package nl.aerius.codegen.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractRoundTripTest extends ParserGeneratorTestBase {
  protected ObjectMapper objectMapper;
  protected File outputDir;
  protected File customDir;
  protected static final String PARSER_PACKAGE = "nl.aerius.codegen.test.generated";

  @BeforeEach
  void setupMapper() throws IOException {
    objectMapper = new ObjectMapper();
    outputDir = new File("src/test/resources/parsers/generated");

    // Clean the output directory
    cleanDirectory(outputDir);

    // Create the directory if it doesn't exist
    outputDir.mkdirs();

    customDir = new File("src/test/resources/parsers/custom");

    System.out.println("\n=== Test Setup ===");
    System.out.println("Output directory: " + outputDir.getAbsolutePath());
    System.out.println("Expected directory: " + new File("src/test/resources/parsers/expected").getAbsolutePath());
    System.out.println("Cleaning up existing files in output directory...");
    System.out.println("Creating output directory: " + outputDir.getAbsolutePath());
  }

  /**
   * Cleans a directory by deleting all files in it.
   * If the directory doesn't exist, it will be created.
   * 
   * @param directory The directory to clean
   */
  private void cleanDirectory(File directory) throws IOException {
    if (directory.exists()) {
      // Delete all files in the directory
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isFile()) {
            if (!file.delete()) {
              throw new IOException("Failed to delete file: " + file.getAbsolutePath());
            }
          }
        }
      }
    }
  }

  protected abstract void prepareParser() throws Exception;

  /**
   * Compiles all parser files in the output directory and loads the specified
   * parser class.
   * 
   * @param className The simple name of the parser class to load (without
   *                  package)
   * @return The loaded parser class
   */
  protected Class<?> compileAndLoadParser(String className) throws Exception {
    // Get the Java compiler
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

    // Find all generated parser files
    List<File> parserFiles = new ArrayList<>(Files.list(outputDir.toPath())
        .filter(path -> path.toString().endsWith(".java"))
        .map(Path::toFile)
        .collect(Collectors.toList()));

    // Add custom parser files that don't already exist in the generated directory
    if (customDir.exists() && customDir.isDirectory()) {
      File[] customFiles = customDir.listFiles((dir, name) -> name.endsWith(".java"));
      if (customFiles != null) {
        for (File customFile : customFiles) {
          File generatedFile = new File(outputDir, customFile.getName());
          if (!generatedFile.exists()) {
            parserFiles.add(customFile);
          }
        }
      }
    }

    if (parserFiles.isEmpty()) {
      throw new IllegalStateException("No parser files found in " + outputDir.getAbsolutePath());
    }

    // Before compiling, replace GWT JSON imports with test implementation in all
    // files
    for (File file : parserFiles) {
      String content = Files.readString(file.toPath());
      String updatedContent = ParserTestUtils.replaceJsonImportsForTesting(content);
      Files.writeString(file.toPath(), updatedContent);
    }

    // Get the classpath including the json-utils module
    String classpath = System.getProperty("java.class.path");

    // Compile all parsers together
    List<String> options = Arrays.asList(
        "-d", outputDir.getAbsolutePath(),
        "-cp", classpath);

    boolean success = compiler.getTask(null, fileManager, null, options, null,
        fileManager.getJavaFileObjectsFromFiles(parserFiles)).call();

    if (!success) {
      throw new IllegalStateException("Failed to compile parser files");
    }

    // Load the requested class
    URLClassLoader classLoader = new URLClassLoader(new URL[] { outputDir.toURI().toURL() });
    return Class.forName(PARSER_PACKAGE + "." + className, true, classLoader);
  }

  /**
   * Finds the appropriate parser class for the given object type.
   * 
   * @param objectType The type of object to find a parser for
   * @return The parser class
   */
  protected Class<?> findParserForType(Class<?> objectType) throws Exception {
    String parserClassName = objectType.getSimpleName() + "Parser";
    return compileAndLoadParser(parserClassName);
  }

  /**
   * Asserts that the object can be serialized to JSON, parsed by the parser, and
   * serialized back to JSON with the same content.
   * 
   * @param original    The original object
   * @param parserClass The parser class
   */
  protected void assertRoundTrip(Object original, Class<?> parserClass) throws Exception {
    // Serialize original object to JSON
    String originalJson = objectMapper.writeValueAsString(original);
    System.out.println("Original JSON: " + originalJson);

    // Find the parse method that takes a String
    Method parseMethod = parserClass.getMethod("parse", String.class);

    // Parse JSON using our parser
    Object parsed = parseMethod.invoke(null, originalJson);

    // Serialize the parsed object back to JSON
    String parsedJson = objectMapper.writeValueAsString(parsed);
    System.out.println("  Parsed JSON: " + parsedJson);

    // Compare the JSON trees to ignore formatting differences
    JsonNode originalTree = objectMapper.readTree(originalJson);
    JsonNode parsedTree = objectMapper.readTree(parsedJson);

    assertEquals(
        originalTree,
        parsedTree,
        "JSON should match after round trip.\nOriginal: " + originalJson + "\nParsed: " + parsedJson);
  }
}