package nl.aerius.codegen.analyzer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about a constructor suitable for constructor-based parsing.
 */
public class ConstructorInfo {
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
