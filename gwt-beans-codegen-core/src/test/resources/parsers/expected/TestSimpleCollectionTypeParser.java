package nl.overheid.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.annotation.processing.Generated;

import nl.overheid.aerius.codegen.test.json.JSONObjectHandle;
import nl.overheid.aerius.codegen.test.types.TestSimpleCollectionType;

@Generated(value = "nl.overheid.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestSimpleCollectionTypeParser {
  public static TestSimpleCollectionType parse(String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestSimpleCollectionType parse(JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }

    final TestSimpleCollectionType config = new TestSimpleCollectionType();

    // Parse sanity
    if (obj.has("sanity")) {
      config.setSanity(obj.getString("sanity"));
    }

    // Parse tags
    if (obj.has("tags") && !obj.isNull("tags")) {
      config.setTags(new ArrayList<>(obj.getStringArray("tags")));
    }

    // Parse metadata
    if (obj.has("metadata") && !obj.isNull("metadata")) {
      final JSONObjectHandle mapObj = obj.getObject("metadata");
      final LinkedHashMap<String, String> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> map.put(key, mapObj.getString(key)));
      config.setMetadata(map);
    }

    // Parse explicitArrayList
    if (obj.has("explicitArrayList") && !obj.isNull("explicitArrayList")) {
      config.setExplicitArrayList(new ArrayList<>(obj.getStringArray("explicitArrayList")));
    }

    // Parse explicitHashMap
    if (obj.has("explicitHashMap") && !obj.isNull("explicitHashMap")) {
      final JSONObjectHandle mapObj = obj.getObject("explicitHashMap");
      final HashMap<String, Integer> map = new HashMap<>();
      mapObj.keySet().forEach(key -> map.put(key, mapObj.getInteger(key)));
      config.setExplicitHashMap(map);
    }

    // Parse explicitLinkedHashMap
    if (obj.has("explicitLinkedHashMap") && !obj.isNull("explicitLinkedHashMap")) {
      final JSONObjectHandle mapObj = obj.getObject("explicitLinkedHashMap");
      final LinkedHashMap<String, Double> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> map.put(key, mapObj.getNumber(key)));
      config.setExplicitLinkedHashMap(map);
    }

    // Parse defaultHashSet
    if (obj.has("defaultHashSet") && !obj.isNull("defaultHashSet")) {
      config.setDefaultHashSet(new HashSet<>(obj.getIntegerArray("defaultHashSet")));
    }

    // Parse explicitHashSet
    if (obj.has("explicitHashSet") && !obj.isNull("explicitHashSet")) {
      config.setExplicitHashSet(new HashSet<>(obj.getStringArray("explicitHashSet")));
    }

    return config;
  }
}
