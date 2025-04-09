package nl.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestSimpleCollectionType;
import nl.aerius.json.JSONArrayHandle;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestSimpleCollectionTypeParser {
  public static TestSimpleCollectionType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestSimpleCollectionType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final TestSimpleCollectionType config = new TestSimpleCollectionType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestSimpleCollectionType config) {
    if (baseObj == null || config == null) {
      return;
    }

    // Parse sanity
    if (baseObj.has("sanity") && !baseObj.isNull("sanity")) {
      final String value = baseObj.getString("sanity");
      config.setSanity(value);
    }

    // Parse tags
    if (baseObj.has("tags") && !baseObj.isNull("tags")) {
      final JSONArrayHandle array = baseObj.getArray("tags");
      final List<String> list = new ArrayList<>();
      array.forEachString(list::add);
      config.setTags(list);
    }

    // Parse metadata
    if (baseObj.has("metadata") && !baseObj.isNull("metadata")) {
      final JSONObjectHandle obj = baseObj.getObject("metadata");
      final Map<String, String> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final String level2Value = obj.getString(key);
        map.put(key, level2Value);
      });
      config.setMetadata(map);
    }

    // Parse explicitArrayList
    if (baseObj.has("explicitArrayList") && !baseObj.isNull("explicitArrayList")) {
      final JSONArrayHandle array = baseObj.getArray("explicitArrayList");
      final ArrayList<String> list = new ArrayList<>();
      array.forEachString(list::add);
      config.setExplicitArrayList(list);
    }

    // Parse explicitHashMap
    if (baseObj.has("explicitHashMap") && !baseObj.isNull("explicitHashMap")) {
      final JSONObjectHandle obj = baseObj.getObject("explicitHashMap");
      final HashMap<String, Integer> map = new HashMap<>();
      obj.keySet().forEach(key -> {
        final Integer level2Value = obj.getInteger(key);
        map.put(key, level2Value);
      });
      config.setExplicitHashMap(map);
    }

    // Parse explicitLinkedHashMap
    if (baseObj.has("explicitLinkedHashMap") && !baseObj.isNull("explicitLinkedHashMap")) {
      final JSONObjectHandle obj = baseObj.getObject("explicitLinkedHashMap");
      final LinkedHashMap<String, Double> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final Double level2Value = obj.getNumber(key);
        map.put(key, level2Value);
      });
      config.setExplicitLinkedHashMap(map);
    }

    // Parse defaultHashSet
    if (baseObj.has("defaultHashSet") && !baseObj.isNull("defaultHashSet")) {
      final JSONArrayHandle array = baseObj.getArray("defaultHashSet");
      final Set<Integer> set = new HashSet<>();
      array.forEachInteger(set::add);
      config.setDefaultHashSet(set);
    }

    // Parse explicitHashSet
    if (baseObj.has("explicitHashSet") && !baseObj.isNull("explicitHashSet")) {
      final JSONArrayHandle array = baseObj.getArray("explicitHashSet");
      final HashSet<String> set = new HashSet<>();
      array.forEachString(set::add);
      config.setExplicitHashSet(set);
    }

  }
}
