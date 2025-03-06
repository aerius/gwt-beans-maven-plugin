package nl.overheid.aerius.codegen.analyzer;

/**
 * Exception thrown when the parser generator encounters a type that it does not support.
 * This provides a clear error message to users about which types cannot be handled.
 */
public class UnsupportedTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new UnsupportedTypeException with a message about the unsupported type.
   * 
   * @param typeName The name of the unsupported type
   * @param fieldName The name of the field containing the unsupported type
   * @param containingClass The class containing the field with the unsupported type
   */
  public UnsupportedTypeException(String typeName, String fieldName, Class<?> containingClass) {
    super(String.format("Unsupported type '%s' found in field '%s' of class '%s'", 
        typeName, fieldName, containingClass.getName()));
  }

  /**
   * Creates a new UnsupportedTypeException with a message about the unsupported type.
   * 
   * @param type The unsupported type class
   * @param fieldName The name of the field containing the unsupported type
   * @param containingClass The class containing the field with the unsupported type
   */
  public UnsupportedTypeException(Class<?> type, String fieldName, Class<?> containingClass) {
    this(type.getName(), fieldName, containingClass);
  }
} 