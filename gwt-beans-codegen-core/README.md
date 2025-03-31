# GWT Bean Parser Generator - Core

## Overview

The GWT Bean Parser Generator is a specialized tool designed to automate the generation of JSON parsers for Java beans. This project helps you migrate from GWT-RPC to a more modern JSON-based API approach while keeping your existing Java bean models.

## Motivation

The project addresses several key challenges when moving away from GWT-RPC:

1. **Decoupling from GWT-RPC**: Reduce dependencies and simplify server setups by moving to standard JSON.
2. **Improved Development Experience**: Eliminate shared project requirements that complicate server setups and development environments.
3. **Better Interoperability**: Make API responses interpretable and interceptable, unlike GWT-RPC's opaque format.
4. **Future-Proofing**: Facilitate potential future migration away from GWT on the client side by reducing coupling.
5. **Gradual Migration**: Allow piece-by-piece migration of your codebase without breaking existing functionality.

## Features

The generated parsers support:

- Primitive types (boolean, byte, short, int, long, float, double, char)
- Object types (Boolean, Byte, Short, Integer, Long, Float, Double, Character, String)
- Arrays (primitive and object arrays)
- Collections (List, Set)
- Maps (with String and Enum keys)
- Nested objects
- Enums
- Custom parsers for special types

### Type Support Examples

```java
class Example {
    // Basic types
    private String string;
    private int primitiveInt;
    private Integer wrapperInt;

    // Collections
    private List<String> stringList;
    private Set<CustomObject> objectSet;

    // Maps
    private Map<String, CustomObject> stringKeyMap;
    private Map<EnumType, String> enumKeyMap;

    // Arrays
    private int[] primitiveArray;
    private String[] objectArray;

    // Nested structures
    private List<Map<String, CustomObject>> nestedListMap;
}
```

## Usage

The code generator can be used standalone or through Maven (recommended). For standalone use:

```bash
java -cp ... nl.aerius.codegen.ParserGenerator \
  --root-class com.example.MyClass \
  --output-dir src/generated/java \
  --parser-package com.example.parser \
  [--custom-parser-dir src/main/java/custom/parsers]
```

### Configuration Parameters

- `--root-class`: The fully qualified name of the root class to generate parsers for
- `--output-dir`: Directory where generated parsers will be written
- `--parser-package`: Package name for the generated parsers
- `--custom-parser-dir`: (Optional) Directory containing custom parser implementations

## Requirements

- Java 17 or higher
- Maven 3.6 or higher (if using through Maven)

## Troubleshooting

### Common Issues

1. **Parser Generation Fails**

   - Check if all required types are GWT-compatible
   - Verify no unsupported types are used
   - Ensure all dependencies are available

2. **Performance Issues**

   - Monitor memory usage with large type hierarchies
   - Consider breaking up deeply nested structures
   - Use appropriate collection types for your use case

3. **Integration Problems**
   - Verify Maven configuration
   - Check generated code location
   - Ensure proper package structure

### Getting Help

- Check the [architecture documentation](architecture.md)
- Review the [development plan](development-plan.md)
- Open an issue on GitHub

## Next Steps

Check the parent project's README for information about the Maven plugin that makes integration into your build process seamless.
