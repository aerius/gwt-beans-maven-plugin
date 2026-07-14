package nl.aerius.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test implementation of JSONObjectHandle that uses Jackson's JsonNode.
 * This allows us to test parsers without modifying the original
 * JSONObjectHandle class.
 */
public class JSONObjectHandle {
  private static final JsonMapper MAPPER = JsonMapper.builder().build();
  private final JsonNode inner;

  public JSONObjectHandle(final JsonNode inner) {
    this.inner = inner;
  }

  public JsonNode getInner() {
    return inner;
  }

  public JSONObjectHandle getObject(final String key) {
    if (isNull(key)) {
      return null;
    }
    final JSONObjectHandle obj = getValue(key).isObject();
    if (obj == null) {
      throw new IllegalStateException(
          "Wrongly assumed json value to be Object while it was not: [" + key + "] in " + inner);
    }
    return obj;
  }

  public Optional<JSONObjectHandle> getObjectOptional(final String key) {
    if (has(key) && get(key).isObject()) {
      return Optional.of(getObject(key));
    } else {
      return Optional.empty();
    }
  }

  public String getString(final String key) {
    final JSONValue value = getValue(key);
    if (value.isNull()) {
      return null;
    }
    final JSONString string = value.isString();
    if (string == null) {
      throw new IllegalStateException(
          "Wrongly assumed json value to be String while it was not: [" + key + "] in " + inner);
    }
    return string.stringValue();
  }

  public Optional<String> getStringOptional(final String key) {
    if (has(key) && get(key).isString()) {
      return Optional.of(getString(key));
    } else {
      return Optional.empty();
    }
  }

  public String getStringOrDefault(final String key, final String devault) {
    try {
      return new JSONObjectHandle(inner).getString(key);
    } catch (final IllegalStateException e) {
      return devault;
    }
  }

  public List<String> getStringArray(final String key) {
    final List<String> lst = new ArrayList<>();
    getArray(key).forEachString(lst::add);
    return lst;
  }

  public List<Double> getNumberArray(final String key) {
    final List<Double> lst = new ArrayList<>();
    getArray(key).forEachNumber(lst::add);
    return lst;
  }

  public List<Integer> getIntegerArray(final String key) {
    final List<Integer> lst = new ArrayList<>();
    getArray(key).forEachInteger(lst::add);
    return lst;
  }

  public JSONArrayHandle getArray(final String key) {
    final JSONArrayHandle array = getValue(key).isArray();
    if (array == null) {
      throw new IllegalStateException(
          "Wrongly assumed json value to be an array while it was not: [" + key + "] in " + inner);
    }
    return array;
  }

  public JSONValue getValue(final String key) {
    final JsonNode value = inner.get(key);
    if (value == null) {
      throw new IllegalStateException("Did not encounter required field in object: " + key + " from " + inner);
    }
    return new JSONValue(value);
  }

  public Double getNumber(final String key) {
    final JSONValue value = getValue(key);
    if (value.isNull()) {
      return 0.0;
    }
    final JSONNumber number = value.isNumber();
    if (number == null) {
      throw new IllegalStateException(
          "Wrongly assumed json value to be Number while it was not: [" + key + "] in " + inner);
    }
    return number.doubleValue();
  }

  public int getInteger(final String key) {
    return getNumber(key).intValue();
  }

  public long getLong(final String key) {
    return getNumber(key).longValue();
  }

  public Set<String> keySet() {
    if (inner.isObject()) {
      final Collection<String> fieldNames = inner.propertyNames();
      return new java.util.HashSet<>(fieldNames);
    }
    return Set.of();
  }

  public JSONValueHandle get(final String key) {
    final JsonNode value = inner.get(key);
    if (value == null) {
      throw new IllegalStateException("Did not encounter required item in object: " + key + " from " + inner);
    }
    return new JSONValueHandle(value);
  }

  public boolean getBoolean(final String key) {
    final JSONBoolean bool = getValue(key).isBoolean();
    if (bool == null) {
      throw new IllegalStateException(
          "Wrongly assumed json value to be Boolean while it was not: [" + key + "] in " + inner);
    }
    return bool.booleanValue();
  }

  public boolean has(final String key) {
    return getInner() != null && keySet() != null && keySet().contains(key);
  }

  public Optional<JSONArrayHandle> getArrayOptional(final String key) {
    if (has(key) && get(key).isArray()) {
      return Optional.of(getArray(key));
    } else {
      return Optional.empty();
    }
  }

  public Optional<Boolean> getBooleanOrDefault(final String key, final boolean devault) {
    if (has(key) && get(key).isBoolean()) {
      return Optional.of(getBoolean(key));
    } else {
      return Optional.of(devault);
    }
  }

  public Optional<Double> getNumberOptional(final String key) {
    if (has(key) && get(key).isNumber()) {
      return Optional.of(getNumber(key));
    } else {
      return Optional.empty();
    }
  }

  public Optional<Integer> getIntegerOptional(final String key) {
    if (has(key) && get(key).isNumber()) {
      return Optional.of(getInteger(key));
    } else {
      return Optional.empty();
    }
  }

  public static JSONObjectHandle fromJson(final JSONValue json) {
    return new JSONObjectHandle(json.getInner());
  }

  public static JSONObjectHandle fromText(final String text) {
    try {
      final JsonNode node = MAPPER.readTree(text);
      return new JSONObjectHandle(node);
    } catch (final Exception e) {
      throw new RuntimeException("Failed to parse JSON: " + text, e);
    }
  }

  public boolean isNull(final String key) {
    return get(key).isNull();
  }

  public String asString() {
    if (!inner.isString()) {
      throw new IllegalStateException("Cannot convert non-string value to string");
    }
    return inner.asString();
  }
}