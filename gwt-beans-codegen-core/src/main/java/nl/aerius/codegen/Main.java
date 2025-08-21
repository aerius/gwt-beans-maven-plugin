/*
 * Copyright the State of the Netherlands
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package nl.aerius.codegen;

import nl.aerius.codegen.generator.ParserWriterUtils;
import nl.aerius.codegen.util.ClassFinder;
import nl.aerius.codegen.util.Logger;

/**
 * Main class to generate parsers from the command line.
 */
public class Main {
  private static final String HELP_TEXT = """
      Usage: ParserGenerator [options]
      Required Options:
      --root-class, -r <class>    Fully qualified name of the root class to generate parsers for
      --output-dir, -o <dir>      Directory where generated parsers will be written
      --parser-package, -p <pkg>  Package name for the generated parsers

      Optional Options:
      --custom-parser-dir, -c <dir> Directory containing custom parsers
      --help, -h                 Show this help message
      """;

  public static void main(final String[] args) {
    System.out.println("\n=== GWT Bean Parser Generator ===");

    System.out.println("\nStep 0: Command line arguments");
    System.out.println("Arguments received:");
    for (int i = 0; i < args.length; i++) {
      System.out.println("  " + i + ": " + args[i]);
    }
    System.out.println();

    final CommandLineOptions options = parseCommandLineArguments(args);
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
      final Logger logger = new Logger() {};
      final ClassFinder classFinder = new ClassFinder() {};

      ParserWriterUtils.initParsers(classFinder, logger);
      ParserGenerator.generateParsers(options.rootClassName, options.outputDir, options.parserPackage, options.customParserDir, classFinder, logger);
      System.out.println("\n✓ Parser generation completed successfully");
    } catch (final Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  private static CommandLineOptions parseCommandLineArguments(final String[] args) {
    final CommandLineOptions options = new CommandLineOptions();
    boolean hasRootClass = false;
    boolean hasOutputDir = false;
    boolean hasParserPackage = false;

    for (int i = 0; i < args.length; i++) {
      final String arg = args[i];
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
    System.out.println(HELP_TEXT);
  }

  private static class CommandLineOptions {
    String rootClassName;
    String outputDir;
    String parserPackage;
    String customParserDir = null;
  }
}
