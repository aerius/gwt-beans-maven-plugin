package nl.aerius.codegen.test.types.polymorphic;

import java.util.Objects;

public class TestPolySubB extends TestPolyBase {

    private static final long serialVersionUID = 1L;

    private boolean fieldB;

    public TestPolySubB() {
        super();
    }

    public TestPolySubB(String baseField, boolean fieldB) {
        super(baseField);
        this.fieldB = fieldB;
    }

    public boolean isFieldB() {
        return fieldB;
    }

    public void setFieldB(boolean fieldB) {
        this.fieldB = fieldB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestPolySubB)) return false;
        if (!super.equals(o)) return false;
        TestPolySubB that = (TestPolySubB) o;
        return isFieldB() == that.isFieldB();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isFieldB());
    }
} 