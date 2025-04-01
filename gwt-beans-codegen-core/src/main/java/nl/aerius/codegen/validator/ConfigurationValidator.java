package nl.aerius.codegen.validator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.ClassName;

import jsinterop.annotations.JsProperty;
import nl.aerius.codegen.analyzer.TypeAnalyzer;
import nl.aerius.codegen.util.FileUtils;

public class ConfigurationValidator {
  private static final String PARSER_PACKAGE = "generated.parsers";
  private static final String CUSTOM_SUBDIR = "custom";
  private static final String WUI_CLIENT_OUTPUT_DIR = FileUtils.getWuiClientOutputDir();
  private static final String GREEN_CHECK = "✅";
  private static final String RED_CROSS = "❌";
  private static final String WARNING = "⚠️";

  private final Set<Class<?>> processedTypes = new HashSet<>();
  private final Set<String> customParserTypes = new HashSet<>();
  private final Set<String> skippedTypes = new HashSet<>();
  private final TypeAnalyzer typeAnalyzer = new TypeAnalyzer();
  private final Set<Class<?>> validatedClasses = new HashSet<>();
  private boolean hasErrors = false;

  public void setCustomParserTypes(Set<String> customParserTypes) {
    this.customParserTypes.clear();
    if (customParserTypes != null) {
      this.customParserTypes.addAll(customParserTypes);
    }
  }

  public void addSkippedType(String fullyQualifiedClassName) {
    skippedTypes.add(fullyQualifiedClassName);
  }

  public void addSkippedType(Class<?> clazz) {
    skippedTypes.add(clazz.getName());
  }

  private boolean shouldSkip(Class<?> type) {
    return type == null ||
        hasCustomParser(type) ||
        skippedTypes.contains(type.getName());
  }

  public boolean isSkipped(Class<?> type) {
    return shouldSkip(type);
  }

  public boolean isSkipped(String fullyQualifiedClassName) {
    return skippedTypes.contains(fullyQualifiedClassName);
  }

  private boolean hasCustomParser(Class<?> type) {
    return customParserTypes.contains(type.getSimpleName());
  }

  public boolean validate(Class<?> type) {
    if (shouldSkip(type)) {
      System.out
          .println("Skipping validation for " + (type != null ? type.getName() : "null") + " (explicitly skipped)");
      return true;
    }

    if (!processedTypes.add(type)) {
      return true; // Already processed
    }

    System.out.println("=== Starting validation of " + type.getName() + " ===");
    validatedClasses.clear();
    hasErrors = false;
    findCustomParsers();

    // Use TypeAnalyzer to discover all types that need validation
    final Set<ClassName> types = typeAnalyzer.analyzeClass(type.getName());
    for (ClassName typeName : types) {
      try {
        // Skip primitive array types
        if (typeName.packageName().isEmpty()) {
          // Extract base type by removing all array brackets
          final String baseTypeName = typeName.simpleName().replaceAll("\\[+\\]", "");
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
        final Class<?> clazz = Class.forName(typeName.packageName() + "." + typeName.simpleName().replace("_", "$"));
        validateClass(clazz);
      } catch (ClassNotFoundException e) {
        System.out
            .println(RED_CROSS + " Could not find class: " + typeName.packageName() + "." + typeName.simpleName());
        hasErrors = true;
      }
    }

    if (!hasErrors) {
      System.out.println(GREEN_CHECK + " " + type.getName() + ": All validation checks passed");
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
          .println(
              WARNING + " " + clazz.getName() + ": Interface type detected (TODO)");
      return;
    }

    // Generic types are not allowed
    if (clazz.getTypeParameters().length > 0) {
      System.out
          .println(WARNING + " " + clazz.getName()
              + ": Generic type parameters are not supported - Remove type parameters like <T>");
      return;
    }

    // Inner classes are not allowed
    if (clazz.isMemberClass()) {
      System.out
          .println(RED_CROSS + " " + clazz.getName() + ": Inner classes are not supported - Move class to top level");
      hasErrors = true;
      return;
    }

    boolean classHasErrors = false;

    // Check for public no-args constructor
    classHasErrors |= !validateConstructor(clazz);

    // Check for getters without corresponding fields
    validateGettersWithoutFields(clazz);

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

  private void validateGettersWithoutFields(final Class<?> clazz) {
    // Get all methods
    for (Method method : clazz.getDeclaredMethods()) {
      // Skip if not public
      if (!Modifier.isPublic(method.getModifiers())) {
        continue;
      }

      // Skip if annotated with @JsonIgnore
      if (method.isAnnotationPresent(JsonIgnore.class)) {
        continue;
      }

      String methodName = method.getName();
      // Check if it's a getter (starts with 'get' or 'is' and has no parameters)
      if ((methodName.startsWith("get") || methodName.startsWith("is")) &&
          method.getParameterCount() == 0 &&
          !method.getReturnType().equals(void.class)) {

        String expectedFieldName;
        if (methodName.startsWith("get")) {
          expectedFieldName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        } else { // starts with "is"
          expectedFieldName = methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
        }

        // Try to find corresponding field
        try {
          Field field = clazz.getDeclaredField(expectedFieldName);
          // Skip if field exists and is private
          if (Modifier.isPrivate(field.getModifiers())) {
            continue;
          }
        } catch (NoSuchFieldException e) {
          // Field doesn't exist
          System.out.println(WARNING + " " + clazz.getName() + ": Getter '" + methodName +
              "' has no corresponding private field '" + expectedFieldName + "'");
        }
      }
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

      // Check if this is a Map field
      if (Map.class.isAssignableFrom(field.getType())) {
        final Type[] genericTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        final Type keyType = genericTypes[0];

        // Skip primitive types, their wrappers, String, and enums as they don't need
        // validation
        if (keyType instanceof Class<?>) {
          final Class<?> keyClass = (Class<?>) keyType;
          if (keyClass.isPrimitive() ||
              keyClass.equals(String.class) ||
              keyClass.isEnum() ||
              keyClass.equals(Integer.class) ||
              keyClass.equals(Long.class) ||
              keyClass.equals(Double.class) ||
              keyClass.equals(Float.class) ||
              keyClass.equals(Boolean.class) ||
              keyClass.equals(Byte.class) ||
              keyClass.equals(Short.class) ||
              keyClass.equals(Character.class)) {
            return isValid;
          }

          // Check for toStringValue implementation or JsonKey annotation
          boolean hasJsonKey = false;
          for (Method method : keyClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(JsonKey.class)) {
              hasJsonKey = true;
              break;
            }
          }

          if (!hasJsonKey) {
            try {
              final Method toStringValueMethod = keyClass.getMethod("toStringValue");
              if (!Modifier.isPublic(toStringValueMethod.getModifiers())) {
                System.out.println(WARNING + " " + field.getDeclaringClass().getName() +
                    ": Map key type '" + keyClass.getName() +
                    "' should use @JsonKey annotation instead of toStringValue method");
                System.out.println(RED_CROSS + " " + field.getDeclaringClass().getName() +
                    ": Map key type '" + keyClass.getName() +
                    "' must have a public toStringValue() method or @JsonKey annotation");
                hasErrors = true;
                return false;
              }
            } catch (NoSuchMethodException e) {
              System.out.println(RED_CROSS + " " + field.getDeclaringClass().getName() +
                  ": Map key type '" + keyClass.getName() +
                  "' must have either a toStringValue() method or @JsonKey annotation");
              hasErrors = true;
              return false;
            }
          }

          // Check for fromStringValue implementation or JsonCreator annotation
          boolean hasJsonCreator = false;
          for (Method method : keyClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(JsonCreator.class)) {
              hasJsonCreator = true;
              break;
            }
          }

          if (!hasJsonCreator) {
            try {
              final Method fromStringValueMethod = keyClass.getMethod("fromStringValue", String.class);
              if (!Modifier.isStatic(fromStringValueMethod.getModifiers())) {
                System.out.println(WARNING + " " + field.getDeclaringClass().getName() +
                    ": Map key type '" + keyClass.getName() +
                    "' should use @JsonCreator annotation instead of fromStringValue method");
                System.out.println(RED_CROSS + " " + field.getDeclaringClass().getName() +
                    ": Map key type '" + keyClass.getName() +
                    "' must have a static fromStringValue(String) method or @JsonCreator annotation");
                hasErrors = true;
                return false;
              }
            } catch (NoSuchMethodException e) {
              System.out.println(RED_CROSS + " " + field.getDeclaringClass().getName() +
                  ": Map key type '" + keyClass.getName() +
                  "' must have either a static fromStringValue(String) method or @JsonCreator annotation");
              hasErrors = true;
              return false;
            }
          }
        }
      }

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