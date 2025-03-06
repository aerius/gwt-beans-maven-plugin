package nl.overheid.aerius.codegen.validator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.ClassName;

import jsinterop.annotations.JsProperty;
import nl.overheid.aerius.codegen.analyzer.TypeAnalyzer;
import nl.overheid.aerius.codegen.util.FileUtils;

public class ConfigurationValidator {
  private static final String PARSER_PACKAGE = "generated.parsers";
  private static final String CUSTOM_SUBDIR = "custom";
  private static final String WUI_CLIENT_OUTPUT_DIR = FileUtils.getWuiClientOutputDir();
  private static final String GREEN_CHECK = "✅";
  private static final String RED_CROSS = "❌";

  private final Set<String> customParserTypes = new HashSet<>();
  private final TypeAnalyzer typeAnalyzer = new TypeAnalyzer();
  private final Set<Class<?>> validatedClasses = new HashSet<>();
  private boolean hasErrors = false;

  public boolean validate(final Class<?> rootClass) {
    System.out.println("=== Starting validation of " + rootClass.getName() + " ===");
    customParserTypes.clear();
    validatedClasses.clear();
    hasErrors = false;
    findCustomParsers();

    // Use TypeAnalyzer to discover all types that need validation
    final Set<ClassName> types = typeAnalyzer.analyzeClass(rootClass.getName());
    for (ClassName type : types) {
      try {
        // Skip primitive array types
        if (type.packageName().isEmpty()) {
          // Extract base type by removing all array brackets
          final String baseTypeName = type.simpleName().replaceAll("\\[+\\]", "");
          // Skip if it's a primitive type
          if (baseTypeName.equals("double") ||
              baseTypeName.equals("int") ||
              baseTypeName.equals("long") ||
              baseTypeName.equals("float") ||
              baseTypeName.equals("boolean") ||
              baseTypeName.equals("byte") ||
              baseTypeName.equals("short") ||
              baseTypeName.equals("char")) {
            continue;
          }
        }
        final Class<?> clazz = Class.forName(type.packageName() + "." + type.simpleName().replace("_", "$"));
        validateClass(clazz);
      } catch (ClassNotFoundException e) {
        System.out.println(RED_CROSS + " Could not find class: " + type.packageName() + "." + type.simpleName());
        hasErrors = true;
      }
    }

    if (!hasErrors) {
      System.out.println(GREEN_CHECK + " " + rootClass.getName() + ": All validation checks passed");
    }
    return !hasErrors;
  }

  private void findCustomParsers() {
    final Path customPath = Paths.get(WUI_CLIENT_OUTPUT_DIR, PARSER_PACKAGE.replace('.', File.separatorChar),
        CUSTOM_SUBDIR);
    if (!Files.exists(customPath)) {
      return;
    }

    try (Stream<Path> files = Files.list(customPath)) {
      files.filter(path -> path.toString().endsWith("Parser.java"))
          .forEach(path -> {
            final String fileName = path.getFileName().toString();
            final String typeName = fileName.substring(0, fileName.length() - 11);
            customParserTypes.add(typeName);
            System.out.println("Found custom parser for: " + typeName);
          });
    } catch (IOException e) {
      System.out.println("Warning: Could not scan for custom parsers: " + e.getMessage());
    }
  }

  private boolean hasCustomParser(final Class<?> clazz) {
    return customParserTypes.contains(clazz.getSimpleName());
  }

  private void validateClass(final Class<?> clazz) {
    if (!validatedClasses.add(clazz)) {
      return; // Already validated this class
    }

    if (clazz.isArray()) {
      // For arrays, validate the component type instead
      validateClass(clazz.getComponentType());
      return;
    }

    if (clazz.isPrimitive() ||
        clazz.equals(String.class) ||
        clazz.isEnum() ||
        clazz.equals(Integer.class) ||
        clazz.equals(Long.class) ||
        clazz.equals(Double.class) ||
        clazz.equals(Float.class) ||
        clazz.equals(Boolean.class) ||
        clazz.getName().startsWith("java.") ||
        hasCustomParser(clazz)) {
      return;
    }

    // First validate superclass if it exists
    final Class<?> superclass = clazz.getSuperclass();
    if (superclass != null && !superclass.equals(Object.class)) {
      validateClass(superclass);
    }

    // Interfaces are not allowed
    if (clazz.isInterface()) {
      System.out
          .println("⚠️ " + clazz.getName() + ": Interface type detected - Consider using a concrete class instead");
      return;
    }

    boolean classHasErrors = false;

    // Check for public no-args constructor
    classHasErrors |= !validateConstructor(clazz);

    // Check all fields
    for (Field field : clazz.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
        continue;
      }

      classHasErrors |= !validateField(clazz, field);
      classHasErrors |= !validateGetterSetter(clazz, field);
      classHasErrors |= !validateFieldType(field);
    }

    if (!classHasErrors) {
      System.out.println(GREEN_CHECK + " " + clazz.getName() + ": All validations passed");
    }
  }

  private boolean validateConstructor(final Class<?> clazz) {
    try {
      final Constructor<?> constructor = clazz.getConstructor();
      if (!Modifier.isPublic(constructor.getModifiers())) {
        System.out.println(RED_CROSS + " " + clazz.getName() + ": Must have a public no-args constructor");
        hasErrors = true;
        return false;
      }
      return true;
    } catch (NoSuchMethodException e) {
      System.out.println(RED_CROSS + " " + clazz.getName() + ": Must have a public no-args constructor");
      hasErrors = true;
      return false;
    }
  }

  private boolean validateField(final Class<?> clazz, final Field field) {
    boolean isValid = true;
    if (!Modifier.isPrivate(field.getModifiers())) {
      System.out.println(RED_CROSS + " " + clazz.getName() + ": Field '" + field.getName() + "' should be private");
      hasErrors = true;
      isValid = false;
    }

    // Check if collection types have @JsProperty annotation (required for VueGWT)
    if ((Collection.class.isAssignableFrom(field.getType()) ||
        Map.class.isAssignableFrom(field.getType()) ||
        field.getType().equals(List.class) ||
        field.getType().equals(Set.class)) &&
        !field.isAnnotationPresent(JsProperty.class)) {
      System.out.println("⚠️ " + clazz.getName() + ": Collection field '" + field.getName()
          + "' missing @JsProperty annotation - This may cause issues with VueGWT");
    }

    return isValid;
  }

  private boolean validateGetterSetter(final Class<?> clazz, final Field field) {
    final String fieldName = field.getName();
    final String capitalizedName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    boolean isValid = true;

    // Check for either public getter or @JsonProperty
    final boolean hasJsonProperty = field.isAnnotationPresent(JsonProperty.class);
    boolean hasValidGetter = false;

    try {
      final Method getter = clazz.getMethod("get" + capitalizedName);
      hasValidGetter = Modifier.isPublic(getter.getModifiers());
    } catch (NoSuchMethodException e) {
      // Try isGetter for boolean
      if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
        try {
          final Method isGetter = clazz.getMethod("is" + capitalizedName);
          hasValidGetter = Modifier.isPublic(isGetter.getModifiers());
        } catch (NoSuchMethodException e2) {
          hasValidGetter = false;
        }
      }
    }

    if (!hasValidGetter && !hasJsonProperty) {
      System.out.println(RED_CROSS + " " + clazz.getName() + ": Field '" + fieldName
          + "' must have either a public getter or @JsonProperty annotation");
      hasErrors = true;
      isValid = false;
    }

    // Always check setter
    try {
      final Method setter = clazz.getMethod("set" + capitalizedName, field.getType());
      if (!Modifier.isPublic(setter.getModifiers())) {
        System.out.println(RED_CROSS + " " + clazz.getName() + ": Field '" + fieldName + "' must have a public setter");
        hasErrors = true;
        isValid = false;
      }
    } catch (NoSuchMethodException e) {
      System.out.println(RED_CROSS + " " + clazz.getName() + ": Field '" + fieldName + "' must have a public setter");
      hasErrors = true;
      isValid = false;
    }

    return isValid;
  }

  private boolean validateFieldType(final Field field) {
    try {
      final boolean isValid = true;

      // Process all types in the field
      FileUtils.analyzeFieldType(field, type -> {
        if (type instanceof Class<?>) {
          validateClass((Class<?>) type);
        }
      });
      return isValid;
    } catch (TypeNotPresentException e) {
      System.out.println(RED_CROSS + " " + field.getDeclaringClass().getName() + ": Type not found: " + e.typeName());
      hasErrors = true;
      return false;
    }
  }
}