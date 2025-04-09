package nl.aerius.codegen.test.types.polymorphic;

import java.util.Objects;

public class TestPolySubA extends TestPolyBase {

    private static final long serialVersionUID = 1L;

    private int fieldA;

    public TestPolySubA() {
        super();
    }

    public TestPolySubA(String baseField, int fieldA) {
        super(baseField);
        this.fieldA = fieldA;
    }

    public int getFieldA() {
        return fieldA;
    }

    public void setFieldA(int fieldA) {
        this.fieldA = fieldA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestPolySubA)) return false;
        if (!super.equals(o)) return false;
        TestPolySubA that = (TestPolySubA) o;
        return getFieldA() == that.getFieldA();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFieldA());
    }
} 