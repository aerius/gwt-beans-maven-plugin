package nl.aerius.codegen.test.types;

/**
 * Abstract middle class in the inheritance hierarchy.
 * Extends BaseType and adds a middle field.
 */
public abstract class AbstractMiddleType extends BaseType {
  private String middle;

  public String getMiddle() {
    return middle;
  }

  public void setMiddle(String middle) {
    this.middle = middle;
  }

  public static AbstractMiddleType createFullObject() {
    ConcreteType obj = new ConcreteType();
    obj.setDeepest("inherited deepest value");
    obj.setMiddle("middle value");
    return obj;
  }
} 