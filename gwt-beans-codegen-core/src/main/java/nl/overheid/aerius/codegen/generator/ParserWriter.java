package nl.overheid.aerius.codegen.generator;

import java.io.IOException;
import java.util.Set;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeSpec;

/**
 * Handles the generation of parser classes for Java types.
 * Responsible for creating parser class files from Java types.
 */
public class ParserWriter {
  private final String outputDir;
  private final String parserPackage;
  private final String generatedTimestamp;

  public ParserWriter(final String outputDir, final String parserPackage) {
    this(outputDir, parserPackage, java.time.LocalDateTime.now().toString());
  }

  public ParserWriter(final String outputDir, final String parserPackage, final String generatedTimestamp) {
    this.outputDir = outputDir;
    this.parserPackage = parserPackage;
    this.generatedTimestamp = generatedTimestamp;
  }

  /**
   * Generates a parser for a specific class.
   */
  public void generateParser(final Class<?> targetClass) throws IOException {
    final String className = targetClass.getSimpleName();
    final String parserClassName = className + "Parser";

    System.out.println("Generating " + parserClassName);

    // Create the parser type specification
    final TypeSpec.Builder typeSpec = ParserWriterUtils.createParserTypeSpec(parserClassName, generatedTimestamp);

    // Add parser methods
    ParserWriterUtils.generateParserForFields(typeSpec, targetClass, parserPackage);

    // Write to file
    ParserWriterUtils.writeParserToFile(outputDir, parserPackage, typeSpec.build(), parserClassName);
  }

  /**
   * Generates parsers for all types in the provided set.
   */
  public void generateParsers(final Set<ClassName> classNames) throws IOException {
    for (ClassName className : classNames) {
      try {
        final Class<?> targetClass = Class.forName(className.canonicalName());
        generateParser(targetClass);
      } catch (ClassNotFoundException e) {
        throw new IOException("Failed to load class: " + className.canonicalName(), e);
      }
    }
  }
}