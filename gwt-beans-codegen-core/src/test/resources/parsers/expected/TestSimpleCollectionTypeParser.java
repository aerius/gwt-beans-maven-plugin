package nl.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    if (baseObj == null) {
      return;
    }

    // Parse sanity
    if (baseObj.has("sanity") && !baseObj.isNull("sanity")) {
      config.setSanity(baseObj.getString("sanity"));
    }

    // Parse tags
    if (baseObj.has("tags") && !baseObj.isNull("tags")) {
      config.setTags(new ArrayList<>(baseObj.getStringArray("tags")));
    }

    // Parse metadata
    if (baseObj.has("metadata") && !baseObj.isNull("metadata")) {
      final JSONObjectHandle mapObj = baseObj.getObject("metadata");
      final Map<String, String> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getString(key));
      });
      config.setMetadata(map);
    }

    // Parse explicitArrayList
    if (baseObj.has("explicitArrayList") && !baseObj.isNull("explicitArrayList")) {
      config.setExplicitArrayList(new ArrayList<>(baseObj.getStringArray("explicitArrayList")));
    }

    // Parse explicitHashMap
    if (baseObj.has("explicitHashMap") && !baseObj.isNull("explicitHashMap")) {
      final JSONObjectHandle mapObj = baseObj.getObject("explicitHashMap");
      final HashMap<String, Integer> map = new HashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getInteger(key));
      });
      config.setExplicitHashMap(map);
    }

    // Parse explicitLinkedHashMap
    if (baseObj.has("explicitLinkedHashMap") && !baseObj.isNull("explicitLinkedHashMap")) {
      final JSONObjectHandle mapObj = baseObj.getObject("explicitLinkedHashMap");
      final LinkedHashMap<String, Double> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, mapObj.getNumber(key));
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
