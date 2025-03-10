package nl.aerius.codegen.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.aerius.codegen.ParserGenerator;
import nl.aerius.codegen.test.types.TestUnsupportedTypesType;

/**
 * Tests that the parser generator correctly fails when encountering unsupported
 * types.
 * This test verifies that the generator throws an appropriate exception for
 * EACH unsupported type,
 * not just the first one it encounters.
 */
class UnsupportedTypeTest extends ParserGeneratorTestBase {
  private static final String TEST_CLASSES_DIR = "src/test/resources/parsers/generated/test-classes";
  private static final List<String> UNSUPPORTED_FIELDS = Arrays.asList(
      "optionalString",
      "localDate",
      "localDateTime",
      "bigDecimal",
      "bigInteger",
      "uuid",
      "date",
      "calendar",
      "sortedSet",
      "treeSet",
      "sortedMap",
      "treeMap");

  @BeforeEach
  void setupTestClassesDirectory() throws IOException {
    // Create test classes directory if it doesn't exist
    Path testClassesPath = Path.of(TEST_CLASSES_DIR);
    if (Files.exists(testClassesPath)) {
      // Clean up any existing test class files
      Files.walk(testClassesPath)
          .filter(Files::isRegularFile)
          .forEach(path -> {
            try {
              Files.delete(path);
            } catch (IOException e) {
              throw new RuntimeException("Failed to delete test class file: " + path, e);
            }
          });
    }
    Files.createDirectories(testClassesPath);
  }

  @Test
  void shouldFailForEachUnsupportedType() {
    // For each field in TestUnsupportedTypesType, create a test class with just
    // that field
    for (String fieldName : UNSUPPORTED_FIELDS) {
      // Find the field and its type
      Field field = findField(fieldName);
      String typeName = field.getType().getSimpleName();

      // Create a test class with just this field
      String testClassName = "Test" + typeName + "Type";
      String fullClassName = generateTestClass(testClassName, field);

      // Test that generating a parser for this class fails
      Exception exception = assertThrows(
          Exception.class,
          () -> ParserGenerator.generateParsers(fullClassName, outputDir.toString(), TEST_PACKAGE),
          "Expected generateParsers to throw an exception for type: " + typeName);

      // Verify the exception message mentions this specific type
      String exceptionMessage = exception.getMessage();
      assertTrue(
          exceptionMessage.contains(field.getType().getName()) ||
              exceptionMessage.contains(field.getType().getSimpleName()),
          "Exception message should mention type '" + typeName + "': " + exceptionMessage);
    }
  }

  private Field findField(String fieldName) {
    try {
      return TestUnsupportedTypesType.class.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("Could not find field: " + fieldName, e);
    }
  }

  private String generateTestClass(String className, Field field) {
    String fullClassName = TEST_PACKAGE + "." + className;

    // Generate a test class with just this field
    String code = String.format(
        "package %s;\n\n" +
            "import %s;\n\n" +
            "public class %s {\n" +
            "    private %s %s;\n\n" +
            "    public %s get%s() {\n" +
            "        return %s;\n" +
            "    }\n\n" +
            "    public void set%s(%s %s) {\n" +
            "        this.%s = %s;\n" +
            "    }\n" +
            "}\n",
        TEST_PACKAGE,
        field.getType().getName(),
        className,
        field.getType().getName(),
        field.getName(),
        field.getType().getName(),
        capitalize(field.getName()),
        field.getName(),
        capitalize(field.getName()),
        field.getType().getName(),
        field.getName(),
        field.getName(),
        field.getName());

    // Write the test class to a file
    try {
      Path sourcePath = Path.of(TEST_CLASSES_DIR, className + ".java");
      Files.writeString(sourcePath, code);

      // Compile the test class
      javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
      compiler.run(null, null, null, sourcePath.toString());

      return fullClassName;
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate test class: " + className, e);
    }
  }

  private String capitalize(String str) {
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }
}