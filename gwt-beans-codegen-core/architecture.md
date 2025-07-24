# Architecture

## System Components

### Core Components

#### 1. Parser Generator

The main orchestrator (`ParserGenerator`) that coordinates the generation process:

- Accepts configuration parameters (root class, output directory, parser package, custom parser directory)
- Initiates type analysis via TypeAnalyzer
- Manages the generation workflow
- Handles file output and directory cleanup
- Provides command-line interface with argument parsing
- Supports version and git hash tracking for @Generated annotations

#### 2. Type Analyzer

The `TypeAnalyzer` component performs deep inspection of Java classes:

- Discovers all types requiring parser generation
- Handles type hierarchies and dependencies
- Filters out primitive types and built-in classes
- Manages type name collisions
- Supports polymorphic type discovery via @JsonSubTypes
- Validates unsupported types and throws appropriate exceptions
- Tracks custom parser types to avoid generation conflicts

#### 3. Parser Writers

Specialized writers handle different aspects of parser generation:

##### ParserWriter

- Main writer that coordinates parser generation for all discovered types
- Manages output directory and file writing
- Handles generator metadata (name, version, git hash)
- Creates parser class specifications with proper annotations

##### ParserWriterUtils

- Shared utilities for parser generation
- Code block generation for different field types
- File I/O operations
- Type conversion utilities
- Common parsing patterns
- Custom parser registry management
- Polymorphic parser generation logic

#### 4. Field Parsers

Specialized parsers for different field types:

- **SimpleFieldParser**: Handles primitives and wrappers (String, int, boolean, etc.)
- **EnumFieldParser**: Handles enum types with @JsonCreator support
- **CollectionFieldParser**: Handles List, Set, and object arrays
- **MapFieldParser**: Handles Map types with String/Enum/complex keys
- **PrimitiveArrayFieldParser**: Handles primitive arrays (int[], double[], etc.)
- **CustomObjectFieldParser**: Handles custom objects requiring generated parsers

#### 5. Configuration Validator

The `ConfigurationValidator` ensures type compatibility:

- Validates class structure and annotations
- Checks for polymorphic type requirements (@JsonTypeInfo, @JsonSubTypes)
- Validates field types and key types for maps
- Ensures proper constructor availability
- Validates getter/setter pairs
- Provides detailed error reporting with visual indicators

### Generated Parser Structure

Each generated parser follows a consistent pattern:

```java
@Generated(
    value = "nl.aerius.codegen.ParserGenerator",
    comments = "version: x.x.x (git: abc123)"
)
public class TypeNameParser {
    public static TypeName parse(final String jsonText) {
        if (jsonText == null) {
            return null;
        }
        return parse(JSONObjectHandle.fromText(jsonText));
    }

    public static TypeName parse(final JSONObjectHandle obj) {
        if (obj == null) {
            return null;
        }
        final TypeName config = new TypeName();
        parse(obj, config);
        return config;
    }

    public static void parse(final JSONObjectHandle obj, final TypeName config) {
        if (obj == null || config == null) {
            return;
        }
        // Field parsing logic with null checks
    }
}
```

For polymorphic types, the main parse method includes switch-based type discrimination:

```java
public static BaseType parse(final JSONObjectHandle obj) {
    if (obj == null) {
        return null;
    }
    
    final String typeName = obj.getString("_type");
    switch (typeName) {
    case "TypeA":
        return SubTypeAParser.parse(obj);
    case "TypeB":
        return SubTypeBParser.parse(obj);
    default:
        throw new RuntimeException("Unknown type name '" + typeName + "'");
    }
}
```

## Data Flow

1. **Type Discovery**

   - Root class analysis via reflection
   - Recursive type scanning through fields and generic parameters
   - Polymorphic subtype discovery via @JsonSubTypes
   - Dependency resolution and ordering
   - Custom parser type filtering

2. **Validation**

   - Type compatibility checks
   - Polymorphic annotation validation
   - Constructor and accessor validation
   - Map key type validation
   - Error reporting with visual indicators

3. **Parser Generation**

   - Template selection based on type category
   - Code generation using JavaPoet
   - Field-specific parsing logic generation
   - Polymorphic parser generation for base types
   - Custom parser integration

4. **File Output**
   - Directory cleanup and creation
   - Java file writing with proper formatting
   - Import statement management
   - Custom parser import tracking

## Design Decisions

### 1. Static Parse Methods

- Parsers use static methods for simplicity and performance
- No state maintenance required
- Easy to use in streaming contexts
- Consistent with utility class patterns

### 2. Null Safety

- All parsers handle null inputs gracefully
- Clear null checking patterns with early returns
- Type-safe output with proper null handling
- Consistent null behavior across all parsers

### 3. Error Handling

- Early validation of input types
- Clear error messages with context
- Fail-fast approach for invalid configurations
- UnsupportedTypeException for unsupported types
- Detailed validation reporting with visual indicators

### 4. Custom Parser Integration

The system supports custom parsers for special cases:

```java
@CustomParser
public class CustomTypeParser {
    public static CustomType parse(final JSONObjectHandle obj) {
        // Custom parsing logic
    }
}
```

Integration points:

- Custom parser discovery via directory scanning
- Type resolution and registration
- Import tracking for generated code
- Validation bypass for custom parser types

### 5. Polymorphic Type Support

- Automatic discovery of @JsonSubTypes annotations
- Generation of switch-based type discrimination
- Support for @JsonTypeInfo with NAME discriminator
- Validation of polymorphic type requirements
- Proper subtype parser generation and integration

### 6. Performance Considerations

1. **Type Analysis & Generation**

   - Caching of type information during analysis
   - Lazy loading of dependent types
   - Memory-efficient type hierarchy traversal
   - Optimized template processing
   - Efficient string handling and code generation
   - Minimized object creation during generation

2. **Runtime Performance**
   - Efficient null checking patterns
   - Optimized collection handling with specific forEach methods
   - Memory usage patterns for large collections
   - Performance metrics collection capabilities
   - Memory usage tracking during generation
   - Generation time profiling support

### 7. Testing Strategy

- **Expected Parser Tests**: Validate reference implementation
- **Generated Parser Tests**: Validate generated code functionality
- **Round-trip Tests**: JSON → Object → JSON validation
- **Validation Tests**: Type compatibility and error handling
- **Custom Parser Tests**: Integration and discovery validation
- **Unsupported Type Tests**: Error handling for invalid types

### 8. GWT Compatibility

- Uses nl.aerius.wui.service.json.JSONObjectHandle for GWT compatibility
- Avoids Java 8+ features and unsupported types
- Maintains compatibility with GWT compilation
- Supports GWT-specific JSON handling patterns
