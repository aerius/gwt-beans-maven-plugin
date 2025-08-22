package nl.aerius.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.palantir.javapoet.ClassName;

import nl.aerius.codegen.analyzer.TypeAnalyzer;
import nl.aerius.codegen.generator.ParserWriter;
import nl.aerius.codegen.generator.ParserWriterUtils;
import nl.aerius.codegen.util.ClassFinder;
import nl.aerius.codegen.util.Logger;
import nl.aerius.codegen.validator.ConfigurationValidator;

/**
 * Main entry point for the parser generation process. Orchestrates the type
 * analysis and parser generation by coordinating between the TypeAnalyzer and
 * ParserWriter components.
 */
public class ParserGenerator {

  public static void generateParsers(final String rootClassName, final String outputDir, final String parserPackage)
      throws IOException, ClassNotFoundException {
    generateParsers(rootClassName, outputDir, parserPackage, null, new ClassFinder() {}, new Logger() {});
  }

  /**
   * Entry point that loads the class, discovers custom parsers, validates,
   * determines generator info, and calls the generation logic.
   */
  public static void generateParsers(final String rootClassName, final String outputDir, final String parserPackage,
      final String customParserDir, final ClassFinder classFinder, final Logger logger) throws IOException, ClassNotFoundException {
    // Load the root class
    final Class<?> rootClass = classFinder.forName(rootClassName);

    // Find custom parsers first (if a custom parser directory is provided)
    final Set<String> customParserTypes = new HashSet<>();
    if (customParserDir != null && !customParserDir.isEmpty()) {
      logger.info("Step 1: Discovering custom parsers");
      customParserTypes.addAll(findCustomParsers(customParserDir, logger));
      if (customParserTypes.isEmpty()) {
        logger.info("No custom parsers found in: " + customParserDir);
      }
    }

    // Validate if needed
    logger.info("Step 2: Validating " + rootClass.getName());
    validateConfiguration(rootClass, customParserTypes, classFinder, logger);

    // Generate parsers
    logger.info("Step 3: Generating Parsers");

    // Determine generator info HERE before calling the recursive/detailed method
    final String generatorName = ParserGenerator.class.getName();

    // Call the main generation method with the determined info
    generateParsersForClass(rootClass, parserPackage, outputDir, customParserDir, generatorName, classFinder, logger);
  }

  private static void validateConfiguration(final Class<?> rootClass, final Set<String> customParserTypes, final ClassFinder classFinder,
      final Logger logger) {
    final ConfigurationValidator validator = new ConfigurationValidator(classFinder, logger);
    validator.setCustomParserTypes(customParserTypes);
    if (!validator.validate(rootClass)) {
      throw new IllegalStateException(rootClass.getName() + " validation failed. Please fix the issues before generating parsers.");
    }
  }

  /**
   * Generates parsers for the given target class and all its dependencies.
   *
   * @param targetClass     The root class to start analysis from.
   * @param parserPackage   The package name for the generated parsers.
   * @param outputDir       The directory where generated source files will be written.
   * @param customParserDir Optional directory containing custom parser implementations.
   * @param generatorName   The name of the generator tool for the @Generated annotation's 'value' element.
   * @param generatorDetails Additional details (version/hash) for the @Generated annotation's 'comments' element.
   * @param classFinder
   * @throws IOException If an error occurs during file writing.
   */
  public static void generateParsersForClass(final Class<?> targetClass, final String parserPackage, final String outputDir,
      final String customParserDir, final String generatorName, final ClassFinder classFinder, final Logger logger) throws IOException {
    logger.info("Generating parsers for " + targetClass.getName());
    logger.info("Generator Name: " + generatorName);
    final String generatorDetails = String.format("version: %s (git: %s)", logger.pluginVersion(), logger.gitHash());
    logger.info("Generator Details: " + generatorDetails);

    try {
      // Find custom parsers first (if a custom parser directory is provided)
      final Set<String> customParserTypes = new HashSet<>();
      if (customParserDir != null && !customParserDir.isEmpty()) {
        customParserTypes.addAll(findCustomParsers(customParserDir, logger));
      }

      // Analyze the class to find all types that need parsers
      final TypeAnalyzer analyzer = new TypeAnalyzer(classFinder, logger);
      analyzer.setCustomParserTypes(customParserTypes);
      final Set<ClassName> classNames = analyzer.analyzeClass(targetClass.getName());

      // Filter out types that have custom parsers (this is now redundant since
      // TypeAnalyzer handles it)
      final Set<ClassName> filteredClassNames = new HashSet<>(classNames);

      // Clean up the output directory
      clearOutputDirectory(outputDir);

      // Create a parser writer and generate all parsers
      // Pass the generator name and details (version + hash) to the writer
      final ParserWriter parserWriter = new ParserWriter(outputDir, parserPackage, generatorName, generatorDetails, classFinder, logger);

      parserWriter.generateParsers(classFinder, filteredClassNames);

      logger.info("Parser generation completed successfully!");
    } catch (final nl.aerius.codegen.analyzer.UnsupportedTypeException e) {
      logger.warn("❌ Error: " + e.getMessage());
      logger.warn("");
      logger.warn("The parser generator does not support this type. Please use a custom parser for this type or modify your model.");
      throw e;
    }
  }

  /**
   * Finds custom parsers in the specified directory.
   *
   * @param customParserDir The directory to search for custom parsers
   * @return A set of type names (without "Parser" suffix) that have custom
   *         parsers
   */
  private static Set<String> findCustomParsers(final String customParserDir, final Logger logger) {
    final Set<String> customParserTypes = new HashSet<>();
    final Path customPath = Paths.get(customParserDir);

    if (!Files.exists(customPath)) {
      logger.info("Warning: Custom parser directory does not exist: " + customParserDir);
      return customParserTypes;
    }

    // Clear any previously registered custom parsers
    ParserWriterUtils.clearCustomParserRegistry();

    try (Stream<Path> files = Files.list(customPath)) {
      files.filter(path -> path.toString().endsWith("Parser.java"))
      .forEach(path -> {
        final String fileName = path.getFileName().toString();
        final String typeName = fileName.substring(0, fileName.length() - 11); // Remove "Parser.java"
        customParserTypes.add(typeName);
        logger.info("Found custom parser for: " + typeName);

        // Extract the package name from the file content
        try {
          final String content = Files.readString(path);
          final String packageLine = content.lines()
              .filter(line -> line.trim().startsWith("package "))
              .findFirst()
              .orElse("");

          if (!packageLine.isEmpty()) {
            // Extract package name from "package x.y.z;"
            final String packageName = packageLine.substring(8, packageLine.indexOf(';')).trim();
            // Register the custom parser for import tracking
            ParserWriterUtils.registerCustomParser(typeName, packageName);
            logger.info("Registered custom parser: " + packageName + "." + typeName + "Parser");
          }
        } catch (final IOException e) {
          logger.info("Warning: Could not read custom parser file: " + e.getMessage());
        }
      });
    } catch (final IOException e) {
      logger.info("Warning: Could not scan for custom parsers: " + e.getMessage());
    }

    return customParserTypes;
  }

  private static void clearOutputDirectory(final String outputDir) throws IOException {
    final Path outputPath = Paths.get(outputDir);

    // Create the output directory if it doesn't exist
    Files.createDirectories(outputPath);

    // Delete all existing parser files
    try (Stream<Path> files = Files.list(outputPath)) {
      files.filter(path -> path.toString().endsWith(".java"))
      .forEach(path -> {
        try {
          Files.delete(path);
        } catch (final IOException e) {
          throw new RuntimeException("Failed to delete file: " + path, e);
        }
      });
    }
  }
}
