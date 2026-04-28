package nl.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestConstructorWithGenericsType;
import nl.aerius.json.JSONArrayHandle;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestConstructorWithGenericsTypeParser {
  public static TestConstructorWithGenericsType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestConstructorWithGenericsType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    // Parse tags
    if (!baseObj.has("tags")) {
      throw new RuntimeException("Required field 'tags' is missing");
    }
    final JSONArrayHandle tagsArray = baseObj.getArray("tags");
    final List<String> tags = new ArrayList<>();
    tagsArray.forEachString(tags::add);

    // Parse counts
    if (!baseObj.has("counts")) {
      throw new RuntimeException("Required field 'counts' is missing");
    }
    final JSONObjectHandle obj = baseObj.getObject("counts");
    final Map<String, Integer> counts = new LinkedHashMap<>();
    obj.keySet().forEach(key -> {
      final Integer level2Value = obj.getInteger(key);
      counts.put(key, level2Value);
    });

    // Parse labels
    if (!baseObj.has("labels")) {
      throw new RuntimeException("Required field 'labels' is missing");
    }
    final JSONArrayHandle labelsArray = baseObj.getArray("labels");
    final Set<String> labels = new HashSet<>();
    labelsArray.forEachString(labels::add);

    // Parse sizes
    if (!baseObj.has("sizes")) {
      throw new RuntimeException("Required field 'sizes' is missing");
    }
    int[] sizes = null;
    final JSONArrayHandle sizesJsonArray = baseObj.getArray("sizes");
    if (sizesJsonArray != null) {
      final List<Integer> sizesTempList = new ArrayList<>();
      sizesJsonArray.forEachInteger(sizesTempList::add);
      sizes = sizesTempList.stream().mapToInt(i -> i != null ? i.intValue() : 0).toArray();
    }

    // Parse aliases
    if (!baseObj.has("aliases")) {
      throw new RuntimeException("Required field 'aliases' is missing");
    }
    String[] aliases = null;
    final JSONArrayHandle aliasesJsonArray = baseObj.getArray("aliases");
    if (aliasesJsonArray != null) {
      final List<String> aliasesTempList = new ArrayList<>();
      aliasesJsonArray.forEachString(aliasesTempList::add);
      aliases = aliasesTempList.toArray(new String[0]);
    }

    return new TestConstructorWithGenericsType(tags, counts, labels, sizes, aliases);
  }
}
