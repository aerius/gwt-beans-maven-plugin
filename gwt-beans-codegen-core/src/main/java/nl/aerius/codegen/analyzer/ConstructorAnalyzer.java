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
   * Returns true if there exists a constructor with parameters matching all parseable fields.
   *
   * @param clazz The class to check
   * @return true if constructor-based parsing can be used
   */
  public boolean canUseConstructorBasedParsing(final Class<?> clazz) {
    return findMatchingConstructorInfo(clazz).isPresent();
  }

  /**
   * Finds the constructor and its parameter order for constructor-based parsing.
   * Only returns a match if the class is truly immutable (lacks setters for all fields).
   *
   * @param clazz The class to analyze
   * @return Optional containing constructor info if a matching constructor exists and class is immutable
   */
  public Optional<ConstructorInfo> findMatchingConstructorInfo(final Class<?> clazz) {
    final List<Field> parseableFields = getParseableFields(clazz);
    if (parseableFields.isEmpty()) {
      return Optional.empty();
    }

    // Check if class has setters for all fields - if yes, prefer setter-based parsing
    if (hasSettersForAllFields(clazz, parseableFields)) {
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
      logger.info("Source file not found for " + clazz.getName() + ", falling back to setter-based parsing");
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
          logger.info("Found matching constructor for " + clazz.getName() +
              " with parameters: " + paramNames);
          return Optional.of(new ConstructorInfo(matchingCtor, paramNames, parseableFields));
        }
      }
    }

    return Optional.empty();
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
    // Handle primitives
    if (reflectionType.isPrimitive()) {
      return sourceTypeName.equals(reflectionType.getName());
    }

    // Handle simple name match
    if (sourceTypeName.equals(reflectionType.getSimpleName())) {
      return true;
    }

    // Handle fully qualified name match
    if (sourceTypeName.equals(reflectionType.getName())) {
      return true;
    }

    return false;
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

  /**
   * Contains information about a constructor suitable for constructor-based parsing.
   */
  public static class ConstructorInfo {
    private final Constructor<?> constructor;
    private final List<String> parameterNames;
    private final List<Field> fields;

    public ConstructorInfo(final Constructor<?> constructor, final List<String> parameterNames,
        final List<Field> fields) {
      this.constructor = constructor;
      this.parameterNames = parameterNames;
      this.fields = fields;
    }

    public Constructor<?> getConstructor() {
      return constructor;
    }

    public List<String> getParameterNames() {
      return parameterNames;
    }

    public List<Field> getFields() {
      return fields;
    }

    /**
     * Returns fields in the order they appear in the constructor parameters.
     */
    public List<Field> getFieldsInConstructorOrder() {
      final List<Field> ordered = new ArrayList<>();
      for (final String paramName : parameterNames) {
        for (final Field field : fields) {
          if (field.getName().equals(paramName)) {
            ordered.add(field);
            break;
          }
        }
      }
      return ordered;
    }
  }
}
