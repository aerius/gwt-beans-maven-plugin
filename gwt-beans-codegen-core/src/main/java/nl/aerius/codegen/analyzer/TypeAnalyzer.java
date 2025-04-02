package nl.aerius.codegen.analyzer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.palantir.javapoet.ClassName;

import nl.aerius.codegen.util.FileUtils;

/**
 * Analyzes classes using reflection to discover and categorize types within a
 * class hierarchy.
 * Identifies which types need parser generation while filtering out primitives
 * and enums.
 */
public class TypeAnalyzer {
  private final Set<Class<?>> primitiveWrappers;
  private final Set<Class<?>> unsupportedTypes;
  private final Set<Class<?>> processedTypes;
  private final Set<String> skippedTypes;
  private final Set<ClassName> discoveredTypes;
  private final Set<String> printedTypes = new HashSet<>();
  private final Set<String> customParserTypes = new HashSet<>();

  public TypeAnalyzer() {
    primitiveWrappers = new HashSet<>();
    primitiveWrappers.add(String.class);
    primitiveWrappers.add(Integer.class);
    primitiveWrappers.add(Long.class);
    primitiveWrappers.add(Double.class);
    primitiveWrappers.add(Float.class);
    primitiveWrappers.add(Boolean.class);

    // Define types that are explicitly not supported
    unsupportedTypes = new HashSet<>();
    // Java 8+ Types
    unsupportedTypes.add(Optional.class);
    unsupportedTypes.add(LocalDate.class);
    unsupportedTypes.add(LocalDateTime.class);

    // Complex Number Types
    unsupportedTypes.add(BigDecimal.class);
    unsupportedTypes.add(BigInteger.class);

    // Special Types
    unsupportedTypes.add(UUID.class);
    unsupportedTypes.add(Date.class);
    unsupportedTypes.add(Calendar.class);

    // Collection Limitations
    unsupportedTypes.add(SortedSet.class);
    unsupportedTypes.add(TreeSet.class);
    unsupportedTypes.add(SortedMap.class);
    unsupportedTypes.add(TreeMap.class);

    processedTypes = new HashSet<>();
    skippedTypes = new HashSet<>();
    discoveredTypes = new TreeSet<>(Comparator.comparing(ClassName::toString));
  }

  /**
   * Sets the list of types that have custom parsers.
   * These types will be skipped during analysis.
   *
   * @param customParserTypes The set of type names that have custom parsers
   */
  public void setCustomParserTypes(Set<String> customParserTypes) {
    this.customParserTypes.clear();
    if (customParserTypes != null) {
      this.customParserTypes.addAll(customParserTypes);
    }
  }

  public Set<ClassName> analyzeClass(final String className) {
    try {
      final Class<?> rootClass = Class.forName(className);
      processedTypes.clear();
      skippedTypes.clear();
      discoveredTypes.clear();
      printedTypes.clear();

      analyzeTypeAndSubtypes(rootClass);

      if (!skippedTypes.isEmpty()) {
        System.out.println("\nWarning: The following types were not found and skipped:");
        skippedTypes.forEach(type -> System.out.println("  - " + type));
        System.out.println();
      }

      return discoveredTypes;
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Could not find class: " + className, e);
    }
  }

  private void analyzeTypeAndSubtypes(final Class<?> type) {
    if (!shouldAnalyzeType(type)) {
      return;
    }

    if (!processedTypes.add(type)) {
      return; // Already processed
    }

    // Print the class hierarchy if we haven't seen this type before
    if (!printedTypes.contains(type.getName())) {
      printClassHierarchy(type);
      printedTypes.add(type.getName());
    }

    // If this type has a custom parser, skip adding it for generation
    // but continue analyzing its fields and subtypes
    if (!hasCustomParser(type)) {
      addTypeForGeneration(type);
    } else {
      System.out.println("Skipping parser generation for " + type.getName() + " (has custom parser)");
    }

    // Find and process superclasses
    Class<?> superclass = type.getSuperclass();
    if (superclass != null && superclass != Object.class) {
      analyzeTypeAndSubtypes(superclass);
    }

    // Find and process subtypes
    // Commented out to make subclass generation more deliberate
    /*
    try (ScanResult scanResult = new ClassGraph()
        .enableClassInfo()
        .acceptPackages("nl.aerius", "nl.overheid.aerius")
        .scan()) {
      final List<Class<?>> subtypes = scanResult.getSubclasses(type.getName()).loadClasses();
      subtypes.forEach(this::analyzeTypeAndSubtypes);
    }
    */

    // Always analyze fields, even for types with custom parsers
    // This ensures we discover all types that might need parsers
    for (Field field : type.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
        try {
          analyzeField(field);
        } catch (TypeNotPresentException e) {
          skippedTypes.add(e.typeName());
        }
      }
    }
  }

  private void analyzeField(final Field field) {
    // Check if the field type is unsupported
    Class<?> fieldType = field.getType();
    if (isUnsupportedType(fieldType)) {
      throw new UnsupportedTypeException(fieldType, field.getName(), field.getDeclaringClass());
    }

    // Check for unsupported generic types
    if (field.getGenericType() instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) field.getGenericType();
      for (Type typeArg : paramType.getActualTypeArguments()) {
        if (typeArg instanceof Class<?> && isUnsupportedType((Class<?>) typeArg)) {
          throw new UnsupportedTypeException(
              ((Class<?>) typeArg).getName(),
              field.getName(),
              field.getDeclaringClass());
        }
      }
    }

    FileUtils.analyzeFieldType(field, this::analyzeType);
  }

  private void analyzeType(Type type) {
    if (type instanceof Class<?>) {
      Class<?> classType = (Class<?>) type;
      if (isUnsupportedType(classType)) {
        throw new UnsupportedTypeException(
            classType.getName(),
            "unknown", // We don't have field name context here
            Object.class); // We don't have containing class context here
      }
      analyzeTypeAndSubtypes(classType);
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType paramType = (ParameterizedType) type;
      // Analyze the raw type
      if (paramType.getRawType() instanceof Class<?>) {
        analyzeTypeAndSubtypes((Class<?>) paramType.getRawType());
      }
      // Analyze all type arguments recursively
      for (Type typeArg : paramType.getActualTypeArguments()) {
        analyzeType(typeArg);
      }
    }
  }

  /**
   * Checks if a type is explicitly unsupported by the parser generator.
   * 
   * @param type The type to check
   * @return true if the type is unsupported, false otherwise
   */
  private boolean isUnsupportedType(Class<?> type) {
    return unsupportedTypes.contains(type);
  }

  private boolean shouldAnalyzeType(final Class<?> type) {
    if (type.isArray()) {
      return shouldAnalyzeType(type.getComponentType());
    }
    return !type.isPrimitive()
        && !primitiveWrappers.contains(type)
        && !type.getName().startsWith("java.")
        && !type.equals(Object.class);
  }

  private void addTypeForGeneration(final Class<?> type) {
    // Don't generate parsers for enums, types with custom parsers, or complex key types
    if (type.isEnum() || 
        customParserTypes.contains(type.getSimpleName()) || 
        isComplexKeyType(type)) {
      return;
    }

    if (type.isMemberClass()) {
      final Class<?> enclosingClass = type.getEnclosingClass();
      final String packageName = enclosingClass.getPackage().getName();
      final String className = enclosingClass.getSimpleName() + "_" + type.getSimpleName();
      discoveredTypes.add(ClassName.get(packageName, className));
    } else {
      final String fullName = type.getName();
      final int lastDot = fullName.lastIndexOf('.');
      final String packageName = lastDot > 0 ? fullName.substring(0, lastDot) : "";
      final String className = type.getSimpleName();
      discoveredTypes.add(ClassName.get(packageName, className));
    }
  }

  /**
   * Checks if a type is a complex key type (has @JsonKey and @JsonCreator annotations).
   * These types are used as map keys and don't need their own parser.
   * 
   * @param type The type to check
   * @return true if the type is a complex key type, false otherwise
   */
  private boolean isComplexKeyType(Class<?> type) {
    try {
      boolean hasJsonCreator = false;
      boolean hasFromStringValue = false;
      
      // Check if the class has a method annotated with @JsonKey
      for (Method method : type.getMethods()) {
        if (method.isAnnotationPresent(JsonCreator.class)) {
          hasJsonCreator = true;
        }
        if (method.getName().equals("fromStringValue") &&
            method.getParameterCount() == 1 &&
            method.getParameterTypes()[0].equals(String.class) &&
            Modifier.isStatic(method.getModifiers())) {
          hasFromStringValue = true;
        }
        
        // If we found either JsonCreator or fromStringValue, it's a complex key type
        if (hasJsonCreator || hasFromStringValue) {
          return true;
        }
      }
    } catch (Exception e) {
      // If we can't check the annotations, assume it's not a complex key type
      return false;
    }
    return false;
  }

  private boolean hasCustomParser(final Class<?> type) {
    boolean hasParser = customParserTypes.contains(type.getSimpleName());
    if (hasParser) {
      System.out.println("Found custom parser for type: " + type.getName());
    }
    return hasParser;
  }

  private void printClassHierarchy(final Class<?> type) {
    if (type.isEnum()) {
      return;
    }

    StringBuilder hierarchy = new StringBuilder();
    Class<?> current = type;
    int length = 0;
    while (current != null && current != Object.class) {
      if (hierarchy.length() > 0) {
        hierarchy.append(" -> ");
      }
      hierarchy.append(current.getSimpleName());
      current = current.getSuperclass();
      length++;
    }
    
    if (length > 1) {
      System.out.println("Class hierarchy: " + hierarchy);
    }
  }
}