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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
    // Also set custom parser types on the TypeAnalyzer instance
    this.typeAnalyzer.setCustomParserTypes(customParserTypes);
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
    if (clazz == null ||
        clazz.isPrimitive() ||
        clazz.isEnum() ||
        clazz.getName().startsWith("java.") ||
        hasCustomParser(clazz) || // Using the helper method is safer than checkinggetSimpleName
        isSkipped(clazz)) { // Using the helper method is safer than checking name directly
      return; // Nothing to validate for these types
    }

    // Original logic starts here
    if (!validatedClasses.add(clazz)) {
      return; // Already validated this class
    }

    // Determine if errors should be treated as warnings based on superclass or implemented interfaces
    boolean treatErrorsAsWarnings = false;
    Class<?> parentToCheck = clazz.getSuperclass();
    if (parentToCheck != null && !parentToCheck.equals(Object.class)) {
      if (hasCustomParser(parentToCheck) && parentToCheck.isAnnotationPresent(JsonTypeInfo.class)) {
        treatErrorsAsWarnings = true;
      }
    }
    // Also check implemented interfaces if not already determined
    if (!treatErrorsAsWarnings) {
      for (Class<?> interfaceToCheck : clazz.getInterfaces()) {
        if (hasCustomParser(interfaceToCheck) && interfaceToCheck.isAnnotationPresent(JsonTypeInfo.class)) {
          treatErrorsAsWarnings = true;
          parentToCheck = interfaceToCheck; // Keep track of the parent causing the warning mode
          break; // Found one, no need to check others
        }
      }
    }

    // Log if we are treating errors as warnings
    if (treatErrorsAsWarnings) {
      System.out.println("\u2139\ufe0f  " + clazz.getName() + ": Parent " + parentToCheck.getSimpleName()
          + " has custom parser and @JsonTypeInfo. Subclass validation issues will be treated as warnings.");
    }

    // Flag to track if *this specific class* has any issues (error or warning)
    boolean classHasIssues = false;

    // Check for interfaces without @JsonTypeInfo - this is always an error
    if (clazz.isInterface() && !clazz.isAnnotationPresent(JsonTypeInfo.class)) {
      System.out.println(RED_CROSS + " " + clazz.getName() + ": Interface must be annotated with @JsonTypeInfo for polymorphic handling.");
      hasErrors = true;
      classHasIssues = true;
      // We don't return here, allowing other checks if applicable, though interfaces have limited other checks.
    }

    // Check for interfaces with @JsonTypeInfo but without @JsonSubTypes
    if (clazz.isInterface() && clazz.isAnnotationPresent(JsonTypeInfo.class)
        && !clazz.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonSubTypes.class)) {
      System.out.println(RED_CROSS + " " + clazz.getName()
          + ": Interface with @JsonTypeInfo must also be annotated with @JsonSubTypes to define concrete implementations.");
      hasErrors = true;
      classHasIssues = true;
    }

    if (clazz.isArray()) {
      // For arrays, validate the component type instead
      validateClass(clazz.getComponentType());
      return;
    }

    // First validate superclass (recursive calls will handle their own error/warning logic)
    final Class<?> superclassToValidate = clazz.getSuperclass();
    if (superclassToValidate != null && !superclassToValidate.equals(Object.class)) {
      validateClass(superclassToValidate);
    }

    // Generic types are not allowed
    if (clazz.getTypeParameters().length > 0) {
      System.out
          .println(WARNING + " " + clazz.getName()
              + ": Generic type parameters are not supported - Remove type parameters like <T>");
      // This is just a warning, don't set classHasIssues or return
    }

    // Inner classes are not allowed
    if (clazz.isMemberClass()) {
      String prefix = treatErrorsAsWarnings ? WARNING : RED_CROSS;
      System.out
          .println(prefix + " " + clazz.getName() + ": Inner classes are not supported - Move class to top level");
      if (!treatErrorsAsWarnings) { // Only set hasErrors if it's a real error
        hasErrors = true;
      }
      classHasIssues = true;
      return; // Still return as this is a fundamental issue
    }

    // Check for public no-args constructor (Skip for interfaces)
    if (!clazz.isInterface()) {
      if (!validateConstructor(clazz, treatErrorsAsWarnings)) {
        classHasIssues = true;
      }
    }

    // Check for getters without corresponding fields
    validateGettersWithoutFields(clazz); // This only prints warnings, doesn't set hasErrors

    // Check all fields
    for (Field field : clazz.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
        continue;
      }

      if (!validateField(clazz, field, treatErrorsAsWarnings)) {
        classHasIssues = true;
      }
      if (!validateGetterSetter(clazz, field, treatErrorsAsWarnings)) {
        classHasIssues = true;
      }
      if (!validateFieldType(clazz, field, treatErrorsAsWarnings)) { // Pass class for context
        classHasIssues = true;
      }
    }

    // Print summary message only if no errors *or* warnings were found for this specific class
    if (!classHasIssues) {
      System.out.println(GREEN_CHECK + " " + clazz.getName() + ": All validations passed for this class");
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

  private boolean validateConstructor(final Class<?> clazz, boolean treatErrorsAsWarnings) {
    try {
      final Constructor<?> constructor = clazz.getConstructor();
      if (!Modifier.isPublic(constructor.getModifiers())) {
        String prefix = treatErrorsAsWarnings ? WARNING : RED_CROSS;
        System.out.println(prefix + " " + clazz.getName() + ": Must have a public no-args constructor (constructor not public)");
        if (!treatErrorsAsWarnings) {
          hasErrors = true;
        }
        return false;
      }
      return true;
    } catch (NoSuchMethodException e) {
      String prefix = treatErrorsAsWarnings ? WARNING : RED_CROSS;
      System.out.println(prefix + " " + clazz.getName() + ": Must have a public no-args constructor (constructor not found)");
      if (!treatErrorsAsWarnings) {
        hasErrors = true;
      }
      return false;
    }
  }

  private boolean validateField(final Class<?> clazz, final Field field, boolean treatErrorsAsWarnings) {
    boolean isValid = true;
    if (!Modifier.isPrivate(field.getModifiers())) {
      String prefix = treatErrorsAsWarnings ? WARNING : RED_CROSS;
      System.out.println(prefix + " " + clazz.getName() + ": Field '" + field.getName() + "' should be private");
      if (!treatErrorsAsWarnings) {
        hasErrors = true;
      }
      isValid = false;
    }

    // Check if collection types have @JsProperty annotation (required for VueGWT)
    // This remains a warning regardless of the treatErrorsAsWarnings flag
    if ((Collection.class.isAssignableFrom(field.getType()) ||
        Map.class.isAssignableFrom(field.getType()) ||
        field.getType().equals(List.class) ||
        field.getType().equals(Set.class)) &&
        !field.isAnnotationPresent(JsProperty.class)) {
      System.out.println("⚠️ " + clazz.getName() + ": Collection field '" + field.getName()
          + "' missing @JsProperty annotation - This may cause issues with VueGWT");
      // This specific check does not set hasErrors or affect isValid, it's just informational
    }

    return isValid;
  }

  private boolean validateGetterSetter(final Class<?> clazz, final Field field, boolean treatErrorsAsWarnings) {
    final String fieldName = field.getName();
    final String capitalizedName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    boolean isValid = true;
    String prefix = treatErrorsAsWarnings ? WARNING : RED_CROSS;

    // Check for either public getter or @JsonProperty
    final boolean hasJsonProperty = field.isAnnotationPresent(JsonProperty.class);
    boolean hasValidGetter = false;
    Method foundGetter = null; // Store the getter method if found

    try {
      final Method getter = clazz.getMethod("get" + capitalizedName);
      if (Modifier.isPublic(getter.getModifiers())) {
        hasValidGetter = true;
        foundGetter = getter; // Store the getter
      }
    } catch (NoSuchMethodException e) {
      // Try isGetter for boolean
      if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
        try {
          final Method isGetter = clazz.getMethod("is" + capitalizedName);
          if (Modifier.isPublic(isGetter.getModifiers())) {
            hasValidGetter = true;
            foundGetter = isGetter; // Store the getter
          }
        } catch (NoSuchMethodException e2) {
          hasValidGetter = false;
        }
      }
    }

    if (!hasValidGetter && !hasJsonProperty) {
      System.out.println(prefix + " " + clazz.getName() + ": Field '" + fieldName
          + "' must have either a public getter or @JsonProperty annotation");
      if (!treatErrorsAsWarnings) {
        hasErrors = true;
      }
      isValid = false;
    } else if (hasValidGetter && !hasJsonProperty) {
      // If only a getter is present, check if its return type matches the field type
      if (foundGetter != null && !foundGetter.getReturnType().equals(field.getType())) {
        System.out.println(prefix + " " + clazz.getName() + ": Field '" + fieldName + "' type ("
            + field.getType().getName() + ") does not match public getter '" + foundGetter.getName()
            + "' return type (" + foundGetter.getReturnType().getName() + ")");
        if (!treatErrorsAsWarnings) {
          hasErrors = true;
        }
        isValid = false;
      }
    }

    // Always check setter
    try {
      final Method setter = clazz.getMethod("set" + capitalizedName, field.getType());
      if (!Modifier.isPublic(setter.getModifiers())) {
        System.out.println(prefix + " " + clazz.getName() + ": Field '" + fieldName + "' must have a public setter (setter not public)");
        if (!treatErrorsAsWarnings) {
          hasErrors = true;
        }
        isValid = false;
      }
    } catch (NoSuchMethodException e) {
      System.out.println(prefix + " " + clazz.getName() + ": Field '" + fieldName + "' must have a public setter (setter not found)");
      if (!treatErrorsAsWarnings) {
        hasErrors = true;
      }
      isValid = false;
    }

    return isValid;
  }

  private boolean validateFieldType(final Class<?> declaringClass, final Field field, boolean treatErrorsAsWarnings) { // Added declaringClass param
    boolean fieldTypeIsValid = true; // Use local flag for this specific validation
    String prefix = treatErrorsAsWarnings ? WARNING : RED_CROSS;
    try {
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
            return fieldTypeIsValid;
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
                // Keep JsonKey message as a warning regardless of the flag
                System.out.println(WARNING + " " + declaringClass.getName() +
                    ": Map key type '" + keyClass.getName() +
                    "' should use @JsonKey annotation instead of toStringValue method");
                System.out.println(prefix + " " + declaringClass.getName() +
                    ": Map key type '" + keyClass.getName() +
                    "' must have a public toStringValue() method or @JsonKey annotation (method not public)");
                if (!treatErrorsAsWarnings) {
                  hasErrors = true;
                }
                fieldTypeIsValid = false;
              }
            } catch (NoSuchMethodException e) {
              System.out.println(prefix + " " + declaringClass.getName() +
                  ": Map key type '" + keyClass.getName() +
                  "' must have either a toStringValue() method or @JsonKey annotation (method not found)");
              if (!treatErrorsAsWarnings) {
                hasErrors = true;
              }
              fieldTypeIsValid = false;
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
                // Keep JsonCreator message as a warning regardless of the flag
                System.out.println(WARNING + " " + declaringClass.getName() +
                    ": Map key type '" + keyClass.getName() +
                    "' should use @JsonCreator annotation instead of fromStringValue method");
                System.out.println(prefix + " " + declaringClass.getName() +
                    ": Map key type '" + keyClass.getName() +
                    "' must have a static fromStringValue(String) method or @JsonCreator annotation (method not static)");
                if (!treatErrorsAsWarnings) {
                  hasErrors = true;
                }
                fieldTypeIsValid = false;
              }
            } catch (NoSuchMethodException e) {
              System.out.println(prefix + " " + declaringClass.getName() +
                  ": Map key type '" + keyClass.getName() +
                  "' must have either a static fromStringValue(String) method or @JsonCreator annotation (method not found)");
              if (!treatErrorsAsWarnings) {
                hasErrors = true;
              }
              fieldTypeIsValid = false;
            }
          }
        }
      }

      // Process all types in the field
      FileUtils.analyzeFieldType(field, type -> {
        if (type instanceof Class<?>) {
          // Recursively call validateClass - it will handle its own error/warning logic
          validateClass((Class<?>) type);
        }
      });
      return fieldTypeIsValid; // Return the validity of this specific check
    } catch (TypeNotPresentException e) {
      System.out
          .println(prefix + " " + declaringClass.getName() + ": Type not found: " + e.typeName() + " referenced by field '" + field.getName() + "'");
      if (!treatErrorsAsWarnings) {
        hasErrors = true;
      }
      return false;
    }
  }
}