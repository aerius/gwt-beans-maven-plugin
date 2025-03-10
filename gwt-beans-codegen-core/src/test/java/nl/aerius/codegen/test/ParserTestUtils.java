package nl.aerius.codegen.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;

/**
 * Utilities for testing generated parsers.
 * Handles replacing GWT JSON implementation with test implementation.
 */
public final class ParserTestUtils {
  private static final String GWT_JSON_PACKAGE = "nl.aerius.wui.service.json";
  private static final String TEST_JSON_PACKAGE = "nl.aerius.json";

  private ParserTestUtils() {
    // Utility class, no instantiation
  }

  /**
   * Replaces GWT JSON imports with test implementation imports.
   * This allows testing the generated parsers without GWT dependencies.
   */
  public static String replaceJsonImportsForTesting(String generatedCode) {
    return generatedCode.replace(
        GWT_JSON_PACKAGE + ".JSONObjectHandle",
        TEST_JSON_PACKAGE + ".JSONObjectHandle");
  }

  /**
   * Writes a generated parser to a file, replacing GWT JSON imports with test
   * implementation imports.
   * This should only be used in test code.
   */
  public static void writeTestParser(String outputDir, String parserPackage, TypeSpec typeSpec, String className)
      throws IOException {
    JavaFile javaFile = JavaFile.builder(parserPackage, typeSpec)
        .skipJavaLangImports(true)
        .indent("  ")
        .build();

    String code = replaceJsonImportsForTesting(javaFile.toString());

    Path outputPath = Paths.get(outputDir, className + ".java");
    System.out.println("Writing test parser to: " + outputPath.toAbsolutePath());

    Files.createDirectories(outputPath.getParent());
    Files.writeString(outputPath, code);
  }

  /**
   * Writes a parser file directly to the output directory without creating
   * package directories.
   * This is used for test files that need to be in a flat directory structure.
   */
  public static void writeTestParserFlat(String outputDir, String parserPackage, TypeSpec typeSpec, String className)
      throws IOException {
    JavaFile javaFile = JavaFile.builder(parserPackage, typeSpec)
        .skipJavaLangImports(true)
        .indent("  ")
        .build();

    String code = replaceJsonImportsForTesting(javaFile.toString());

    Path outputPath = Paths.get(outputDir, className + ".java");
    System.out.println("Writing test parser to: " + outputPath.toAbsolutePath());

    Files.createDirectories(Paths.get(outputDir));
    Files.writeString(outputPath, code);
  }
}