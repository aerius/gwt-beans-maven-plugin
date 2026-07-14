package nl.aerius.json;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

import tools.jackson.databind.node.ArrayNode;

public class JSONArrayHandle {
  private final ArrayNode inner;

  public JSONArrayHandle(final ArrayNode inner) {
    this.inner = inner;
  }

  public void forEach(final Consumer<JSONObjectHandle> consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(new JSONObjectHandle(inner.get(i)));
    }
  }

  public void forEachString(final Consumer<String> consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(inner.get(i).asString());
    }
  }

  public void forEachNumber(final DoubleConsumer consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(inner.get(i).asDouble());
    }
  }

  public void forEachInteger(final IntConsumer consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(inner.get(i).asInt());
    }
  }

  public void forEachWithIndex(final ObjIntConsumer<JSONObjectHandle> consumer) {
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(new JSONObjectHandle(inner.get(i)), i);
    }
  }

  public List<JSONObjectHandle> toList() {
    final List<JSONObjectHandle> result = new ArrayList<>();
    for (int i = 0; i < inner.size(); i++) {
      result.add(new JSONObjectHandle(inner.get(i)));
    }
    return result;
  }
}