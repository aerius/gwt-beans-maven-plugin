package nl.overheid.aerius.codegen.test.json;

import com.fasterxml.jackson.databind.JsonNode;

public class JSONValueHandle {
  private final JsonNode inner;

  public JSONValueHandle(final JsonNode inner) {
    this.inner = inner;
  }

  public boolean isObject() {
    return inner.isObject();
  }

  public boolean isArray() {
    return inner.isArray();
  }

  public boolean isString() {
    return inner.isTextual();
  }

  public boolean isNumber() {
    return inner.isNumber();
  }

  public boolean isBoolean() {
    return inner.isBoolean();
  }

  public boolean isNull() {
    return inner.isNull();
  }
}