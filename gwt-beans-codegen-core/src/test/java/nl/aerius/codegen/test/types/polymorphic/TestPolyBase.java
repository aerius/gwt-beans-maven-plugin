package nl.aerius.codegen.test.types.polymorphic;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, property = "_type")
@JsonSubTypes({
    @Type(value = TestPolySubA.class, name = "TypeA"),
    @Type(value = TestPolySubB.class, name = "TypeB")
})
public abstract class TestPolyBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private String baseField;

    protected TestPolyBase() {}

    protected TestPolyBase(String baseField) {
        this.baseField = baseField;
    }

    public String getBaseField() {
        return baseField;
    }

    public void setBaseField(String baseField) {
        this.baseField = baseField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestPolyBase)) return false;
        TestPolyBase that = (TestPolyBase) o;
        return Objects.equals(getBaseField(), that.getBaseField());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBaseField());
    }
} 