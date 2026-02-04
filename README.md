# GWT Beans Maven Plugin

A Maven plugin for generating GWT bean parsers.

## Project Structure

This project consists of the following modules:

1. **gwt-beans-codegen-test-json**: JVM-based test stubs for GWT JSON classes, enabling unit testing without GWT.
2. **gwt-beans-codegen-core**: The core code generation engine for GWT bean parsers.
3. **gwt-beans-codegen-maven-plugin**: The Maven plugin code.

## Building

To build all modules:

```bash
mvn clean install
```

## Modules

### gwt-beans-codegen-test-json

JVM-based test stubs for GWT JSON classes, backed by Jackson. Enables unit testing of generated parsers without a GWT runtime.

See [gwt-beans-codegen-test-json/README.md](gwt-beans-codegen-test-json/README.md) for usage instructions.

### gwt-beans-codegen-core

The core code generation engine for GWT bean parsers.
For more detailed information and how to use the tool via the command line see the module [gwt-beans-codegen-core/README.md](gwt-beans-codegen-core/README.md)

### gwt-beans-codegen-maven-plugin

The Maven plugin to generate the Parsers during generate sources phase.
For more information on how to use and configure the Maven plugin see the module [gwt-beans-codegen-maven-plugin/README.md](gwt-beans-codegen-maven-plugin/README.md)

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
