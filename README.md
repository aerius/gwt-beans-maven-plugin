# GWT Beans Maven Plugin

A Maven plugin for generating GWT bean parsers.

## Project Structure

This project consists of the following modules:

1. **json-utils**: A lightweight JSON handling library built on Jackson.
2. **gwt-beans-codegen-core**: The core code generation engine for GWT bean parsers.
3. **gwt-beans-codegen-maven-plugin**: The Maven plugin code.

## Building

To build all modules:

```bash
mvn clean install
```

## Modules

### json-utils

A lightweight JSON handling library that provides utilities for working with JSON data.

See [json-utils/README.md](json-utils/README.md) for usage instructions.

### gwt-beans-codegen-core

The core code generation engine for GWT bean parsers.
For more detailed information and how to use the tool via the command line see the module [gwt-beans-codegen-core/README.md](gwt-beans-codegen-core/README.md)

### gwt-beans-codegen-maven-plugin

The Maven plugin to generate the Parsers during generate sources phase.
For more information on how to use and configure the Maven plugin see the module [gwt-beans-codegen-maven-plugin/README.md](gwt-beans-codegen-maven-plugin/README.md)

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
