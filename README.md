# GWT Bean Parser Generator

A tool to help migrate away from GWT-RPC by generating JSON parsers for your Java beans. This project helps you transition from GWT-RPC to a more modern JSON-based approach while keeping your existing Java bean models.

## 1. Core Code Generator (`gwt-beans-codegen-core`)

A Java library that generates parser code for Java beans. It:

- Analyzes Java classes to understand their structure
- Generates corresponding parser classes that handle JSON serialization/deserialization
- Supports various field types including primitives, collections, maps, and enums
- Handles complex nested types and custom parsers
- Preserves type safety and null handling
- Can be run standalone with command-line arguments

### Usage

```bash
java -cp ... nl.aerius.codegen.ParserGenerator \
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

## Why Move Away from GWT-RPC?

GWT-RPC, while powerful, has several limitations:

- Tightly couples client and server implementations
- Requires both ends to use GWT
- Limited interoperability with other systems
- No longer actively maintained

This generator helps you migrate to a JSON-based approach while:

- Keeping your existing Java bean models
- Maintaining type safety
- Supporting all common Java types
- Allowing gradual migration
- Enabling interoperability with any JSON-capable client or server

## Features

The generator supports:

- All Java primitive types and their wrappers
- Collections (List, Set, etc.)
- Maps (with both String and Enum keys)
- Enums
- Date/time types
- Nested objects
- Custom parsers for special cases
- Null safety
- Type safety

## Project Status

- Core generator: Functional and tested
- Maven plugin: Under development

## Requirements

- Java 11 or higher
- Maven 3.6 or higher (for the Maven plugin)
