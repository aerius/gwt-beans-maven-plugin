# GWT Beans Maven Plugin

This project consists of two main components that work together to generate GWT-compatible bean parsers:

## 1. Core Code Generator (`gwt-beans-codegen-core`)

A Java library that generates parser code for Java beans. It:

- Analyzes Java classes to understand their structure
- Generates corresponding parser classes that can handle JSON serialization
- Supports various field types including primitives, collections, maps, and enums
- Handles complex nested types and custom parsers
- Can be run standalone with command-line arguments

### Usage

```bash
java -cp ... nl.overheid.aerius.codegen.ParserGenerator \
  --root-class com.example.MyClass \
  --output-dir src/generated/java \
  --parser-package com.example.parser \
  [--custom-parser-dir src/main/java/custom/parsers]
```

## 2. Maven Plugin (Coming Soon)

A Maven plugin that integrates the code generator into the Maven build process. It will:

- Make the full project classpath available to the code generator
- Allow configuration through the Maven POM
- Generate parsers during the build process
- Support incremental builds
- Handle dependencies correctly

### Planned Usage

```xml
<plugin>
  <groupId>nl.overheid.aerius</groupId>
  <artifactId>gwt-beans-maven-plugin</artifactId>
  <version>${project.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>generate-parsers</goal>
      </goals>
      <configuration>
        <rootClass>com.example.MyClass</rootClass>
        <outputDirectory>src/generated/java</outputDirectory>
        <parserPackage>com.example.parser</parserPackage>
        <customParserDirectory>src/main/java/custom/parsers</customParserDirectory>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Project Status

- Core generator: Functional and tested
- Maven plugin: Under development

## Requirements

- Java 11 or higher
- Maven 3.6 or higher (for the Maven plugin)
