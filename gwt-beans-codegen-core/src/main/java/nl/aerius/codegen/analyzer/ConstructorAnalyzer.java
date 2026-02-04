package nl.aerius.codegen.analyzer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;

import nl.aerius.codegen.util.Logger;

/**
 * Analyzes classes to determine if they should use constructor-based parsing.
 * A class uses constructor-based parsing if:
 * 1. It has a constructor whose parameters match all parseable fields by name
 * 2. The class does NOT have setters for all those fields (i.e., it's immutable)
 *
 * If a class has setters for all fields, setter-based parsing is preferred
 * for backwards compatibility.
 */
public class ConstructorAnalyzer {

  private final List<String> sourceRoots;
  private final Logger logger;

  public ConstructorAnalyzer(final List<String> sourceRoots, final Logger logger) {
    this.sourceRoots = sourceRoots != null ? sourceRoots : List.of();
    this.logger = logger;
  }

  /**
   * Checks if a class can use constructor-based parsing.
   * Returns true if there exists a constructor with parameters matching all parseable fields
   * AND the class is immutable (does not have setters for all fields).
   *
   * @param clazz The class to check
   * @return true if constructor-based parsing can be used
   */
  public boolean canUseConstructorBasedParsing(final Class<?> clazz) {
    return findMatchingConstructorInfo(clazz).isPresent();
  }

  /**
   * Checks if a class has a constructor that could be used for initialization.
   * Unlike canUseConstructorBasedParsing(), this does NOT require the class to be immutable.
   * Used for validation to determine if a no-arg constructor is required.
   *
   * @param clazz The class to check
   * @return true if a matching constructor exists
   */
  public boolean hasMatchingConstructor(final Class<?> clazz) {
    return findMatchingConstructorInfo(clazz, false).isPresent();
  }

  /**
   * Finds the constructor and its parameter order for constructor-based parsing.
   * Only returns a match if the class is truly immutable (lacks setters for all fields).
   *
   * @param clazz The class to analyze
   * @return Optional containing constructor info if a matching constructor exists and class is immutable
   */
  public Optional<ConstructorInfo> findMatchingConstructorInfo(final Class<?> clazz) {
    return findMatchingConstructorInfo(clazz, true);
  }

  /**
   * Finds the constructor and its parameter order for constructor-based parsing.
   *
   * @param clazz The class to analyze
   * @param requireImmutable If true, only returns a match if the class lacks setters for all fields
   * @return Optional containing constructor info if a matching constructor exists
   */
  private Optional<ConstructorInfo> findMatchingConstructorInfo(final Class<?> clazz, final boolean requireImmutable) {
    final List<Field> parseableFields = getParseableFields(clazz);
    if (parseableFields.isEmpty()) {
      return Optional.empty();
    }

    // Check if class has setters for all fields - if yes and requireImmutable, prefer setter-based parsing
    if (requireImmutable && hasSettersForAllFields(clazz, parseableFields)) {
      return Optional.empty();
    }

    final Set<String> fieldNames = parseableFields.stream()
        .map(Field::getName)
        .collect(Collectors.toSet());

    // First, find constructors with matching parameter count using reflection
    final List<Constructor<?>> candidates = Arrays.stream(clazz.getDeclaredConstructors())
        .filter(ctor -> ctor.getParameterCount() == parseableFields.size())
        .collect(Collectors.toList());

    if (candidates.isEmpty()) {
      return Optional.empty();
    }

    // Try to find source file and extract parameter names
    final Optional<CompilationUnit> sourceFile = findSourceFile(clazz);
    if (sourceFile.isEmpty()) {
      // Only throw hard error if this class needs constructor-based parsing (no setters)
      // For hasMatchingConstructor() calls (requireImmutable=false), just return empty
      if (requireImmutable) {
        if (sourceRoots.isEmpty()) {
          throw new IllegalStateException("Cannot analyze constructor for " + clazz.getName()
              + ": no source roots configured. Use --source-root option or ensure classpath contains target/classes directories.");
        }
        throw new IllegalStateException("Source file not found for " + clazz.getName()
            + ". Searched in source roots: " + sourceRoots
            + ". Ensure source files are available for constructor-based types.");
      }
      return Optional.empty();
    }

    // Find the class declaration in the source file
    final Optional<ClassOrInterfaceDeclaration> classDecl = sourceFile.get()
        .findFirst(ClassOrInterfaceDeclaration.class,
            c -> c.getNameAsString().equals(clazz.getSimpleName()));

    if (classDecl.isEmpty()) {
      logger.info("Class declaration not found in source for " + clazz.getName());
      return Optional.empty();
    }

    // Find a constructor with parameters matching all field names
    for (final ConstructorDeclaration ctorDecl : classDecl.get().getConstructors()) {
      final List<String> paramNames = ctorDecl.getParameters().stream()
          .map(p -> p.getNameAsString())
          .collect(Collectors.toList());

      // Check if all parameter names match field names
      if (paramNames.size() == fieldNames.size() && fieldNames.containsAll(paramNames)) {
        // Find the matching reflection constructor
        final Constructor<?> matchingCtor = findMatchingReflectionConstructor(clazz, ctorDecl);
        if (matchingCtor != null) {
          // Validate that constructor parameter types match field types
          final String typeMismatch = validateParameterTypes(matchingCtor, paramNames, parseableFields);
          if (typeMismatch != null) {
            logger.warn("Constructor parameter type mismatch in " + clazz.getName() + ": " + typeMismatch);
            continue; // Try next constructor
          }

          logger.info("Found matching constructor for " + clazz.getName() +
              " with parameters: " + paramNames);
          return Optional.of(new ConstructorInfo(matchingCtor, paramNames, parseableFields));
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Validates that constructor parameter types match the corresponding field types.
   *
   * @param ctor The constructor to validate
   * @param paramNames The parameter names in order
   * @param fields The list of parseable fields
   * @return null if all types match, or an error message describing the mismatch
   */
  private String validateParameterTypes(final Constructor<?> ctor, final List<String> paramNames,
      final List<Field> fields) {
    final Class<?>[] paramTypes = ctor.getParameterTypes();

    for (int i = 0; i < paramNames.size(); i++) {
      final String paramName = paramNames.get(i);
      final Class<?> paramType = paramTypes[i];

      // Find the field with this name
      Field matchingField = null;
      for (final Field field : fields) {
        if (field.getName().equals(paramName)) {
          matchingField = field;
          break;
        }
      }

      if (matchingField == null) {
        return "No field found for parameter '" + paramName + "'";
      }

      // Check if types match
      if (!typesMatch(paramType, matchingField.getType())) {
        return "Parameter '" + paramName + "' has type " + paramType.getSimpleName()
            + " but field has type " + matchingField.getType().getSimpleName();
      }
    }

    return null; // All types match
  }

  /**
   * Checks if two types match, handling primitives and their wrappers.
   */
  private boolean typesMatch(final Class<?> paramType, final Class<?> fieldType) {
    // Exact match
    if (paramType.equals(fieldType)) {
      return true;
    }

    // Check primitive/wrapper equivalence
    if (paramType.isPrimitive() || fieldType.isPrimitive()) {
      return getPrimitiveWrapper(paramType).equals(getPrimitiveWrapper(fieldType));
    }

    return false;
  }

  /**
   * Returns the wrapper class for a primitive, or the class itself if not primitive.
   */
  private Class<?> getPrimitiveWrapper(final Class<?> type) {
    if (type == boolean.class) return Boolean.class;
    if (type == byte.class) return Byte.class;
    if (type == char.class) return Character.class;
    if (type == short.class) return Short.class;
    if (type == int.class) return Integer.class;
    if (type == long.class) return Long.class;
    if (type == float.class) return Float.class;
    if (type == double.class) return Double.class;
    return type;
  }

  /**
   * Gets all fields that should be parsed (non-static, non-transient, non-synthetic).
   */
  public static List<Field> getParseableFields(final Class<?> clazz) {
    final List<Field> fields = new ArrayList<>();
    for (final Field field : clazz.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers())
          && !Modifier.isTransient(field.getModifiers())
          && !field.isSynthetic()) {
        fields.add(field);
      }
    }
    return fields;
  }

  /**
   * Checks if a class has setter methods for all parseable fields.
   * If true, setter-based parsing is preferred over constructor-based.
   */
  private boolean hasSettersForAllFields(final Class<?> clazz, final List<Field> fields) {
    for (final Field field : fields) {
      final String setterName = "set" + capitalize(field.getName());
      try {
        clazz.getMethod(setterName, field.getType());
      } catch (final NoSuchMethodException e) {
        // No setter for this field - class is not fully mutable
        return false;
      }
    }
    return true;
  }

  /**
   * Capitalizes the first character of a string.
   */
  private static String capitalize(final String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  /**
   * Finds the reflection Constructor that matches the JavaParser ConstructorDeclaration.
   */
  private Constructor<?> findMatchingReflectionConstructor(final Class<?> clazz,
      final ConstructorDeclaration ctorDecl) {
    final int paramCount = ctorDecl.getParameters().size();

    for (final Constructor<?> ctor : clazz.getDeclaredConstructors()) {
      if (ctor.getParameterCount() == paramCount) {
        // Match by parameter types
        boolean matches = true;
        for (int i = 0; i < paramCount; i++) {
          final String sourceTypeName = ctorDecl.getParameter(i).getType().asString();
          final Class<?> reflectionType = ctor.getParameterTypes()[i];

          if (!typeNamesMatch(sourceTypeName, reflectionType)) {
            matches = false;
            break;
          }
        }
        if (matches) {
          return ctor;
        }
      }
    }
    return null;
  }

  /**
   * Checks if a source type name matches a reflection type.
   */
  private boolean typeNamesMatch(final String sourceTypeName, final Class<?> reflectionType) {
    return sourceTypeName.equals(reflectionType.getName())
        || sourceTypeName.equals(reflectionType.getSimpleName());
  }

  /**
   * Finds the source file for the given class.
   */
  private Optional<CompilationUnit> findSourceFile(final Class<?> clazz) {
    final String relativePath = clazz.getName().replace('.', '/') + ".java";

    for (final String sourceRoot : sourceRoots) {
      final Path sourcePath = Path.of(sourceRoot, relativePath);
      if (sourcePath.toFile().exists()) {
        try {
          return Optional.of(StaticJavaParser.parse(sourcePath));
        } catch (final Exception e) {
          logger.warn("Failed to parse source file: " + sourcePath + " - " + e.getMessage());
        }
      }
    }
    return Optional.empty();
  }

}
