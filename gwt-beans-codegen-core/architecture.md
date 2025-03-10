# Architecture

## System Components

### Core Components

#### 1. Parser Generator

The main orchestrator (`ParserGenerator`) that coordinates the generation process:

- Accepts configuration parameters
- Initiates type analysis
- Manages the generation workflow
- Handles file output

#### 2. Type Analyzer

The `TypeAnalyzer` component performs deep inspection of Java classes:

- Discovers all types requiring parser generation
- Handles type hierarchies and dependencies
- Filters out primitive types and built-in classes
- Manages type name collisions

#### 3. Parser Writers

Two specialized writers handle different aspects of parser generation:

##### Root Parser Writer

- Generates the main entry point parser
- Handles top-level configuration
- Manages the primary parsing logic

##### Sub-Type Parser Writer

- Generates parsers for nested and dependent types
- Handles complex type relationships
- Manages naming conflicts for nested types

#### 4. Parser Writer Utils

Shared utilities for parser generation:

- Code block generation
- File I/O operations
- Type conversion utilities
- Common parsing patterns

### Generated Parser Structure

Each generated parser follows a consistent pattern:

```java
@Generated(
    value = "nl.aerius.codegen.ParserGenerator",
    date = "timestamp"
)
public class TypeNameParser {
    public static TypeName parse(JSONObjectHandle obj) {
        if (obj == null) {
            return null;
        }
        final TypeName config = new TypeName();
        // Field parsing logic
        return config;
    }
}
```

## Data Flow

1. **Type Discovery**

   - Root class analysis
   - Recursive type scanning
   - Dependency resolution

2. **Parser Generation**

   - Template selection
   - Code generation
   - File writing

3. **Validation**
   - Type compatibility checks
   - Name collision detection
   - Output verification

## Design Decisions

### 1. Static Parse Methods

- Parsers use static methods for simplicity
- No state maintenance required
- Easy to use in streaming contexts

### 2. Null Safety

- All parsers handle null inputs gracefully
- Clear null checking patterns
- Type-safe output

### 3. Error Handling

- Early validation of input types
- Clear error messages
- Fail-fast approach for invalid configurations
