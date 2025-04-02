package nl.aerius.codegen.test.types;

/**
 * Concrete class in the inheritance hierarchy.
 * Extends AbstractMiddleType and adds an outer field.
 */
public class ConcreteType extends AbstractMiddleType {
  private String outer;

  public String getOuter() {
    return outer;
  }

  public void setOuter(String outer) {
    this.outer = outer;
  }

  public static ConcreteType createFullObject() {
    ConcreteType obj = new ConcreteType();
    obj.setDeepest("inherited deepest value");
    obj.setMiddle("inherited middle value");
    obj.setOuter("outer value");
    return obj;
  }
} 