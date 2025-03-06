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
   - int[] arrays

3. **Object Support**
   - Custom object references
   - Null handling for all types
   - Custom parser integration

### Remaining Implementation Tasks

1. **Collection Types** [HIGH]

   - List<CustomObject>
   - Set<CustomObject>
   - List<Enum>
   - Collection<T> (other collection types)

2. **Map Types** [HIGH]

   - Map<String, CustomObject>
   - Map<String, List<T>>
   - Map<String, Enum>

3. **Primitive Arrays** [MEDIUM]

   - long[]
   - double[]
   - float[]
   - byte[]
   - short[]
   - char[]
   - boolean[]

4. **Complex Nested Structures** [MEDIUM]

   - List<Map<String, T>>
   - Map<String, List<CustomObject>>

5. **Documentation & Cleanup** [HIGH]
   - Document condition patterns for different field types
   - Review code for redundancy and consistency
   - Update comments to reflect implementation details

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
   - Map with non-String keys
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
