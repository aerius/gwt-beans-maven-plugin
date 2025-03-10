package nl.aerius.json;

import com.fasterxml.jackson.databind.JsonNode;

public class JSONValue {
  private final JsonNode inner;

  public JSONValue(final JsonNode inner) {
    this.inner = inner;
  }

  public JsonNode getInner() {
    return inner;
  }

  public JSONObjectHandle isObject() {
    return inner.isObject() ? new JSONObjectHandle(inner) : null;
  }

  public JSONArrayHandle isArray() {
    return inner.isArray() ? new JSONArrayHandle((com.fasterxml.jackson.databind.node.ArrayNode) inner) : null;
  }

  public JSONString isString() {
    return inner.isTextual() ? new JSONString(inner.asText()) : null;
  }

  public JSONNumber isNumber() {
    return inner.isNumber() ? new JSONNumber(inner.asDouble()) : null;
  }

  public JSONBoolean isBoolean() {
    return inner.isBoolean() ? new JSONBoolean(inner.asBoolean()) : null;
  }

  public boolean isNull() {
    return inner.isNull();
  }
}

class JSONString {
  private final String value;

  public JSONString(String value) {
    this.value = value;
  }

  public String stringValue() {
    return value;
  }
}

class JSONNumber {
  private final double value;

  public JSONNumber(double value) {
    this.value = value;
  }

  public double doubleValue() {
    return value;
  }
}

class JSONBoolean {
  private final boolean value;

  public JSONBoolean(boolean value) {
    this.value = value;
  }

  public boolean booleanValue() {
    return value;
  }
}