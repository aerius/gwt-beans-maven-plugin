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
import nl.aerius.codegen.validator.ConfigurationValidator;

/**
 * Main entry point for the parser generation process. Orchestrates the type
 * analysis and parser generation by coordinating between the TypeAnalyzer and
 * ParserWriter components.
 */
public class ParserGenerator {
  public static void main(String[] args) {
    System.out.println("\n=== GWT Bean Parser Generator ===");

    System.out.println("\nStep 0: Command line arguments");
    System.out.println("Arguments received:");
    for (int i = 0; i < args.length; i++) {
      System.out.println("  " + i + ": " + args[i]);
    }
    System.out.println();

    CommandLineOptions options = parseCommandLineArguments(args);
    if (options == null) {
      return; // Help was displayed or invalid arguments
    }

    System.out.println("\nStep 0: Interpreted arguments");
    System.out.println("  Root class: " + options.rootClassName);
    System.out.println("  Output directory: " + options.outputDir);
    System.out.println("  Parser package: " + options.parserPackage);
    System.out.println(
        "  Custom parser directory: " + (options.customParserDir != null ? options.customParserDir : "not specified"));
    System.out.println();

    try {
      generateParsers(options.rootClassName, options.outputDir, options.parserPackage, options.customParserDir);
      System.out.println("\n✓ Parser generation completed successfully");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  private static CommandLineOptions parseCommandLineArguments(String[] args) {
    CommandLineOptions options = new CommandLineOptions();
    boolean hasRootClass = false;
    boolean hasOutputDir = false;
    boolean hasParserPackage = false;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if ("--help".equals(arg) || "-h".equals(arg)) {
        printUsage();
        return null;
      } else if ("--root-class".equals(arg) || "-r".equals(arg)) {
        if (i + 1 < args.length) {
          options.rootClassName = args[++i];
          hasRootClass = true;
        } else {
          System.err.println("Missing value for --root-class");
          return null;
        }
      } else if ("--output-dir".equals(arg) || "-o".equals(arg)) {
        if (i + 1 < args.length) {
          options.outputDir = args[++i];
          hasOutputDir = true;
        } else {
          System.err.println("Missing value for --output-dir");
          return null;
        }
      } else if ("--parser-package".equals(arg) || "-p".equals(arg)) {
        if (i + 1 < args.length) {
          options.parserPackage = args[++i];
          hasParserPackage = true;
        } else {
          System.err.println("Missing value for --parser-package");
          return null;
        }
      } else if ("--custom-parser-dir".equals(arg) || "-c".equals(arg)) {
        if (i + 1 < args.length) {
          options.customParserDir = args[++i];
        } else {
          System.err.println("Missing value for --custom-parser-dir");
          return null;
        }
      } else {
        System.err.println("Unknown argument: " + arg);
        printUsage();
        return null;
      }
    }

    if (!hasRootClass || !hasOutputDir || !hasParserPackage) {
      System.err.println("Error: --root-class, --output-dir, and --parser-package are required arguments");
      printUsage();
      return null;
    }

    return options;
  }

  private static void printUsage() {
    System.out.println("Usage: ParserGenerator [options]");
    System.out.println("Required Options:");
    System.out.println("  --root-class, -r <class>    Fully qualified name of the root class to generate parsers for");
    System.out.println("  --output-dir, -o <dir>      Directory where generated parsers will be written");
    System.out.println("  --parser-package, -p <pkg>  Package name for the generated parsers");
    System.out.println("\nOptional Options:");
    System.out.println("  --custom-parser-dir, -c <dir> Directory containing custom parsers");
    System.out.println("  --help, -h                  Show this help message");
  }

  public static void generateParsers(String rootClassName, String outputDir, String parserPackage)
      throws IOException, ClassNotFoundException {
    generateParsers(rootClassName, outputDir, parserPackage, null);
  }

  public static void generateParsers(String rootClassName, String outputDir, String parserPackage,
      String customParserDir)
      throws IOException, ClassNotFoundException {
    // Load the root class
    final Class<?> rootClass = Class.forName(rootClassName);

    // Find custom parsers first (if a custom parser directory is provided)
    final Set<String> customParserTypes = new HashSet<>();
    if (customParserDir != null && !customParserDir.isEmpty()) {
      System.out.println("\nStep 1: Discovering custom parsers");
      customParserTypes.addAll(findCustomParsers(customParserDir));
      if (customParserTypes.isEmpty()) {
        System.out.println("No custom parsers found in: " + customParserDir);
      }
    }

    // Validate if needed
    System.out.println("\nStep 2: Validating " + rootClass.getName());
    validateConfiguration(rootClass, customParserTypes);

    // Generate parsers
    System.out.println("\nStep 3: Generating Parsers");
    generateParsersForClass(rootClass, parserPackage, outputDir, customParserDir);
  }

  private static void validateConfiguration(Class<?> rootClass, Set<String> customParserTypes) {
    final ConfigurationValidator validator = new ConfigurationValidator();
    validator.setCustomParserTypes(customParserTypes);
    if (!validator.validate(rootClass)) {
      throw new IllegalStateException(
          rootClass.getName() + " validation failed. Please fix the issues before generating parsers.");
    }
  }

  public static void generateParsersForClass(Class<?> targetClass, String parserPackage, String outputDir)
      throws IOException {
    generateParsersForClass(targetClass, parserPackage, outputDir, null);
  }

  public static void generateParsersForClass(Class<?> targetClass, String parserPackage, String outputDir,
      String customParserDir) throws IOException {
    generateParsersForClass(targetClass, parserPackage, outputDir, customParserDir,
        java.time.LocalDateTime.now().toString());
  }

  public static void generateParsersForClass(Class<?> targetClass, String parserPackage, String outputDir,
      String customParserDir, String timestamp) throws IOException {
    System.out.println("Generating parsers for " + targetClass.getName());

    try {
      // Find custom parsers first (if a custom parser directory is provided)
      final Set<String> customParserTypes = new HashSet<>();
      if (customParserDir != null && !customParserDir.isEmpty()) {
        customParserTypes.addAll(findCustomParsers(customParserDir));
      }

      // Analyze the class to find all types that need parsers
      final TypeAnalyzer analyzer = new TypeAnalyzer();
      analyzer.setCustomParserTypes(customParserTypes);
      final Set<ClassName> classNames = analyzer.analyzeClass(targetClass.getName());

      // Filter out types that have custom parsers (this is now redundant since
      // TypeAnalyzer handles it)
      final Set<ClassName> filteredClassNames = new HashSet<>(classNames);

      // Clean up the output directory
      clearOutputDirectory(outputDir);

      // Create a parser writer and generate all parsers
      final ParserWriter parserWriter = new ParserWriter(outputDir, parserPackage, timestamp);
      parserWriter.generateParsers(filteredClassNames);

      System.out.println("\nParser generation completed successfully!");
    } catch (nl.aerius.codegen.analyzer.UnsupportedTypeException e) {
      System.err.println("\n❌ Error: " + e.getMessage());
      System.err.println(
          "\nThe parser generator does not support this type. Please use a custom parser for this type or modify your model.");
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
  private static Set<String> findCustomParsers(String customParserDir) {
    final Set<String> customParserTypes = new HashSet<>();
    final Path customPath = Paths.get(customParserDir);

    if (!Files.exists(customPath)) {
      System.out.println("Warning: Custom parser directory does not exist: " + customParserDir);
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
            System.out.println("Found custom parser for: " + typeName);

            // Extract the package name from the file content
            try {
              String content = Files.readString(path);
              String packageLine = content.lines()
                  .filter(line -> line.trim().startsWith("package "))
                  .findFirst()
                  .orElse("");

              if (!packageLine.isEmpty()) {
                // Extract package name from "package x.y.z;"
                String packageName = packageLine.substring(8, packageLine.indexOf(';')).trim();
                // Register the custom parser for import tracking
                ParserWriterUtils.registerCustomParser(typeName, packageName);
                System.out.println("Registered custom parser: " + packageName + "." + typeName + "Parser");
              }
            } catch (IOException e) {
              System.out.println("Warning: Could not read custom parser file: " + e.getMessage());
            }
          });
    } catch (IOException e) {
      System.out.println("Warning: Could not scan for custom parsers: " + e.getMessage());
    }

    return customParserTypes;
  }

  private static void clearOutputDirectory(String outputDir) throws IOException {
    final Path outputPath = Paths.get(outputDir);

    // Create the output directory if it doesn't exist
    Files.createDirectories(outputPath);

    // Delete all existing parser files
    try (Stream<Path> files = Files.list(outputPath)) {
      files.filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> {
            try {
              Files.delete(path);
            } catch (IOException e) {
              throw new RuntimeException("Failed to delete file: " + path, e);
            }
          });
    }
  }

  private static class CommandLineOptions {
    String rootClassName;
    String outputDir;
    String parserPackage;
    String customParserDir = null;
  }
}
