package nl.overheid.aerius.json;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class JSONArrayHandle {
  private final ArrayNode inner;

  public JSONArrayHandle(final ArrayNode inner) {
    this.inner = inner;
  }

  public void forEachString(Consumer<String> consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(inner.get(i).asText());
    }
  }

  public void forEachNumber(Consumer<Double> consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(inner.get(i).asDouble());
    }
  }

  public void forEachInteger(Consumer<Integer> consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(inner.get(i).asInt());
    }
  }

  public void forEachWithIndex(BiConsumer<JSONObjectHandle, Integer> consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(new JSONObjectHandle(inner.get(i)), i);
    }
  }

  public List<JSONObjectHandle> toList() {
    List<JSONObjectHandle> result = new ArrayList<>();
    for (int i = 0; i < inner.size(); i++) {
      result.add(new JSONObjectHandle(inner.get(i)));
    }
    return result;
  }
}