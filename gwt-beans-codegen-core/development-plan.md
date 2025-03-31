# Development Plan

## Purpose

We're building a JSON parser generator that will replace GWT-RPC serialization. The generator creates Java classes that can parse JSON into strongly-typed objects.

## Current Status

### Working Features ✓

1. **Basic Types**

   - String, int/Integer, boolean/Boolean, double/Double, long/Long
   - byte/Byte, short/Short, float/Float, char/Character
   - Inner and outer enum types

2. **Collections & Maps**

   - List<String>, ArrayList<String>
   - Set<Integer>, HashSet<String>
   - Map<String, String>, HashMap<String, Integer>
   - LinkedHashMap<String, Double>
   - All primitive arrays (int[], long[], double[], float[], byte[], short[], char[], boolean[])

3. **Object Support**

   - Custom object references
   - Null handling for all types
   - Custom parser integration

4. **Complex Types**
   - List<CustomObject>
   - Set<CustomObject>
   - List<Enum>
   - Collection<T> types
   - Map<String, CustomObject>
   - Map<String, primitive types>
   - Map<Enum, String>
   - Map<Enum, CustomObject>
   - Map<Enum, primitive types>

### Remaining Implementation Tasks

1. **Complex Nested Structures** [HIGH]

   - Deeply nested collections (3+ levels)
   - Examples to implement:
     ```java
     class ComplexNested {
       private List<List<List<CustomObject>>> deepNestedList;
       private Map<String, List<Map<String, List<CustomObject>>>> complexNestedMap;
     }
     ```

2. **Documentation & Cleanup** [HIGH]

   - Document condition patterns for different field types
   - Review code for redundancy and consistency
   - Update comments to reflect implementation details
   - Add usage examples and best practices
   - Document performance considerations

3. **Testing Strategy** [HIGH]

   - Add performance benchmarks for different type combinations
   - Create stress tests for deeply nested structures
   - Add memory usage tests for large collections
   - Implement test coverage reporting
   - Add integration tests with real-world scenarios

4. **Performance Optimization** [MEDIUM]
   - Profile parser generation for large type hierarchies
   - Optimize memory usage during type analysis
   - Cache frequently used type information
   - Optimize generated code for common patterns
   - Add performance monitoring hooks

### Explicitly Unsupported Types

The following types will NOT be supported due to GWT compatibility or design decisions:

1. **Java 8+ Types**

   - Optional<T>
   - java.time.\* (LocalDate, LocalDateTime, etc.)

2. **Complex Number Types**

   - BigDecimal
   - BigInteger

3. **Special Types**

   - UUID
   - Date (java.util.Date)
   - Calendar

4. **Collection Limitations**
   - Map with non-String keys (except enums)
   - Queue and Deque implementations
   - SortedSet/TreeSet
   - SortedMap/TreeMap

## Development Constraints

### 1. Immutable Components (DO NOT MODIFY)

- Test infrastructure (suite files, runners, JSONObjectHandle, utilities)
- Test types (existing fields, class structure)

### 2. Limited Modification Components

- Test Types: Can only add new fields for parser development
- Expected Parser: Can modify for new parsing/fixes, preserve working code

### 3. Freely Modifiable Components

- Generator Code (analyzer, writers, utilities)

### 4. GWT Compatibility

- JSONObjectHandle is Jackson-based test variant
- Must maintain GWT compatibility

## Implementation Notes

- Expected parser serves as reference implementation
- Generated code must match expected output exactly
- Custom parsers sourced from original location
- Uses Jackson for test serialization
- Keep type analysis and parsing logic separate

### Migration Guide

1. **From GWT-RPC**

   - Step-by-step migration process
   - Common pitfalls and solutions
   - Performance comparison guidelines
   - Testing strategy for migrated code

2. **Version Upgrades**
   - Breaking changes documentation
   - Upgrade procedures
   - Compatibility matrix
