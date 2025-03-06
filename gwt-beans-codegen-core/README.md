# AERIUS Calculator Code Generator

## Overview

The AERIUS Calculator Code Generator is a specialized tool designed to automate the generation of JSON parsers for Java classes. This project is part of a larger initiative to migrate the AERIUS Calculator from GWT-RPC to a more modern JSON-based API approach.

## Motivation

The project addresses several key challenges in the AERIUS Calculator:

1. **Decoupling from GWT-RPC**: Moving away from GWT-RPC to reduce dependencies and simplify server setups.
2. **Improved Development Experience**: Eliminating shared project requirements that complicate server setups and development environments.
3. **Better Interoperability**: Making API responses interpretable and interceptable, unlike GWT-RPC's opaque format.
4. **Future-Proofing**: Facilitating potential future migration away from GWT on the client side by reducing coupling.

## Usage

The code generator is primarily used through Maven:

```bash
mvn exec:java
```

This will:

1. Scan the specified root class and its dependencies
2. Generate appropriate JSON parsers for all discovered types
3. Output the generated parsers to the configured directory

### Configuration

The generator can be configured through the following parameters:

- `--root-class`: The fully qualified name of the root class to generate parsers for
- `--output-dir`: Directory where generated parsers will be written
- `--parser-package`: Package name for the generated parsers

## Generated Parser Features

The generated parsers support:

- Primitive types (boolean, byte, short, int, long, float, double, char)
- Object types (Boolean, Byte, Short, Integer, Long, Float, Double, Character)
- Arrays (primitive and object arrays)
- Collections (List, Set)
- Maps (Map<String, T>)
- Nested objects
- Enums
- Date/time types (LocalDate)

## Requirements

- Java 11 or higher
- Maven 3.x
