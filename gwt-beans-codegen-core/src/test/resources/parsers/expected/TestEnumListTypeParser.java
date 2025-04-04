package nl.aerius.codegen.test.generated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Generated;

import nl.aerius.codegen.test.types.TestEnumListType;
import nl.aerius.codegen.test.types.TestEnumType;
import nl.aerius.json.JSONArrayHandle;
import nl.aerius.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestEnumListTypeParser {

  public static TestEnumListType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestEnumListType parse(final JSONObjectHandle baseObj) {
    if (baseObj == null) {
      return null;
    }

    final TestEnumListType config = new TestEnumListType();
    parse(baseObj, config);
    return config;
  }

  public static void parse(final JSONObjectHandle baseObj, final TestEnumListType config) {
    if (baseObj == null) {
      return;
    }

    // Parse statusList
    if (baseObj.has("statusList") && !baseObj.isNull("statusList")) {
      final JSONArrayHandle array = baseObj.getArray("statusList");
      final List<TestEnumType.Status> list = new ArrayList<>();
      array.forEachString(item -> {
        TestEnumType.Status level2Value = null;
        if (item != null) {
          try {
            level2Value = TestEnumType.Status.valueOf(item);
          } catch (IllegalArgumentException e) {
            // Invalid enum value, leave as default;
          }
        }
        list.add(level2Value);
      });
      config.setStatusList(list);
    }

    // Parse statusSet
    if (baseObj.has("statusSet") && !baseObj.isNull("statusSet")) {
      final JSONArrayHandle array = baseObj.getArray("statusSet");
      final Set<TestEnumType.Status> set = new HashSet<>();
      array.forEachString(item -> {
        TestEnumType.Status level2Value = null;
        if (item != null) {
          try {
            level2Value = TestEnumType.Status.valueOf(item);
          } catch (IllegalArgumentException e) {
            // Invalid enum value, leave as default;
          }
        }
        set.add(level2Value);
      });
      config.setStatusSet(set);
    }

    // Parse statusMap
    if (baseObj.has("statusMap") && !baseObj.isNull("statusMap")) {
      final JSONObjectHandle obj = baseObj.getObject("statusMap");
      final Map<String, TestEnumType.Status> map = new LinkedHashMap<>();
      obj.keySet().forEach(key -> {
        final String level2Str = obj.getString(key);
        TestEnumType.Status level2Value = null;
        if (level2Str != null) {
          try {
            level2Value = TestEnumType.Status.valueOf(level2Str);
          } catch (IllegalArgumentException e) {
            // Invalid enum value, leave as default
          }
        }
        map.put(key, level2Value);
      });
      config.setStatusMap(map);
    }

    // Parse description
    if (baseObj.has("description") && !baseObj.isNull("description")) {
      final String value = baseObj.getString("description");
      config.setDescription(value);
    }

  }
}
