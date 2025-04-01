package nl.aerius.codegen.test.generated;

import javax.annotation.processing.Generated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.aerius.codegen.test.types.TestEnumListType;
import nl.aerius.codegen.test.types.TestEnumType;
import nl.aerius.wui.service.json.JSONObjectHandle;

@Generated(value = "nl.aerius.codegen.ParserGenerator", date = "2024-01-01T00:00:00")
public class TestEnumListTypeParser {

  public static TestEnumListType parse(final String jsonText) {
    if (jsonText == null) {
      return null;
    }

    return parse(JSONObjectHandle.fromText(jsonText));
  }

  public static TestEnumListType parse(final JSONObjectHandle obj) {
    if (obj == null) {
      return null;
    }
    final TestEnumListType config = new TestEnumListType();

    // Parse statusList
    if (obj.has("statusList") && !obj.isNull("statusList")) {
      final List<TestEnumType.Status> statusList = new ArrayList<>();
      obj.getArray("statusList").forEachString(str -> {
        statusList.add(TestEnumType.Status.valueOf(str));
      });
      config.setStatusList(statusList);
    }

    // Parse statusSet
    if (obj.has("statusSet") && !obj.isNull("statusSet")) {
      final Set<TestEnumType.Status> statusSet = new HashSet<>();
      obj.getArray("statusSet").forEachString(str -> {
        statusSet.add(TestEnumType.Status.valueOf(str));
      });
      config.setStatusSet(statusSet);
    }

    // Parse statusMap
    if (obj.has("statusMap") && !obj.isNull("statusMap")) {
      final JSONObjectHandle mapObj = obj.getObject("statusMap");
      final Map<String, TestEnumType.Status> map = new LinkedHashMap<>();
      mapObj.keySet().forEach(key -> {
        map.put(key, TestEnumType.Status.valueOf(mapObj.getString(key)));
      });
      config.setStatusMap(map);
    }

    // Parse description
    if (obj.has("description")) {
      config.setDescription(obj.getString("description"));
    }

    return config;
  }
}