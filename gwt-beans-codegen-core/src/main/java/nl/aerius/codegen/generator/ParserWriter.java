package nl.aerius.codegen.generator;

import java.io.IOException;
import java.util.Set;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeSpec;

import nl.aerius.codegen.util.ClassFinder;
import nl.aerius.codegen.util.Logger;

/**
 * Handles the generation of parser classes for Java types.
 * Responsible for creating parser class files from Java types.
 */
public class ParserWriter {
  private final String outputDir;
  private final String parserPackage;
  private final String generatorName;
  private final String generatorDetails;
  private final ClassFinder classFinder;
  private final Logger logger;

  public ParserWriter(final String outputDir, final String parserPackage, final String generatorName,
      final String generatorDetails, final ClassFinder classFinder, final Logger logger) {
    this.outputDir = outputDir;
    this.parserPackage = parserPackage;
    this.generatorName = generatorName;
    this.generatorDetails = generatorDetails;
    this.classFinder = classFinder;
    this.logger = logger;
  }

  /**
   * Generates a parser for a specific class.
   */
  public void generateParser(final Class<?> targetClass) throws IOException {
    final String className = targetClass.getSimpleName();
    final String parserClassName = className + "Parser";

    logger.info("Generating " + parserClassName);

    // Create the parser type specification, passing both name and details
    final TypeSpec.Builder typeSpec = ParserWriterUtils.createParserTypeSpec(parserClassName, generatorName, generatorDetails);

    // Add parser methods
    ParserWriterUtils.generateParserForFields(typeSpec, targetClass, parserPackage, classFinder);

    // Write to file
    ParserWriterUtils.writeParserToFile(outputDir, parserPackage, typeSpec.build(), parserClassName, logger);
  }

  /**
   * Generates parsers for all types in the provided set.
   */
  public void generateParsers(final ClassFinder classFinder, final Set<ClassName> classNames) throws IOException {
    for (final ClassName className : classNames) {
      try {
        final Class<?> targetClass = classFinder.forName(className.canonicalName());

        generateParser(targetClass);
      } catch (final ClassNotFoundException e) {
        throw new IOException("Failed to load class: " + className.canonicalName(), e);
      }
    }
  }
}