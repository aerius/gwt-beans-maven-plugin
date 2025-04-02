package nl.aerius.codegen.test.types;

/**
 * Base class in the inheritance hierarchy.
 * Contains the deepest field in the inheritance chain.
 */
public class BaseType {
  private String deepest;

  public String getDeepest() {
    return deepest;
  }

  public void setDeepest(String deepest) {
    this.deepest = deepest;
  }

  public static BaseType createFullObject() {
    BaseType obj = new BaseType();
    obj.setDeepest("deepest value");
    return obj;
  }
} 