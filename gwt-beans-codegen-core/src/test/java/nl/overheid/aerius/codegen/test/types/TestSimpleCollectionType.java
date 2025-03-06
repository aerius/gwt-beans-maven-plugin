package nl.overheid.aerius.codegen.test.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestSimpleCollectionType {
  private String sanity; // shall be null

  private List<String> tags; // should default to ArrayList
  private Map<String, String> metadata; // should default to LinkedHashMap
  private ArrayList<String> explicitArrayList;
  private HashMap<String, Integer> explicitHashMap;
  private LinkedHashMap<String, Double> explicitLinkedHashMap;
  private Set<Integer> defaultHashSet; // should default to HashSet
  private HashSet<String> explicitHashSet;

  public String getSanity() {
    return sanity;
  }

  public void setSanity(String sanity) {
    this.sanity = sanity;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public ArrayList<String> getExplicitArrayList() {
    return explicitArrayList;
  }

  public void setExplicitArrayList(ArrayList<String> explicitArrayList) {
    this.explicitArrayList = explicitArrayList;
  }

  public HashMap<String, Integer> getExplicitHashMap() {
    return explicitHashMap;
  }

  public void setExplicitHashMap(HashMap<String, Integer> explicitHashMap) {
    this.explicitHashMap = explicitHashMap;
  }

  public LinkedHashMap<String, Double> getExplicitLinkedHashMap() {
    return explicitLinkedHashMap;
  }

  public void setExplicitLinkedHashMap(LinkedHashMap<String, Double> explicitLinkedHashMap) {
    this.explicitLinkedHashMap = explicitLinkedHashMap;
  }

  public Set<Integer> getDefaultHashSet() {
    return defaultHashSet;
  }

  public void setDefaultHashSet(Set<Integer> defaultHashSet) {
    this.defaultHashSet = defaultHashSet;
  }

  public HashSet<String> getExplicitHashSet() {
    return explicitHashSet;
  }

  public void setExplicitHashSet(HashSet<String> explicitHashSet) {
    this.explicitHashSet = explicitHashSet;
  }

  public static TestSimpleCollectionType createFullObject() {
    TestSimpleCollectionType obj = new TestSimpleCollectionType();
    obj.setSanity("sanity");
    obj.setTags(new ArrayList<>(List.of("tag1", "tag2", "tag3")));
    obj.setMetadata(new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")));

    obj.setExplicitArrayList(new ArrayList<>(List.of("explicit1", "explicit2")));

    HashMap<String, Integer> hashMap = new HashMap<>();
    hashMap.put("one", 1);
    hashMap.put("two", 2);
    obj.setExplicitHashMap(hashMap);

    LinkedHashMap<String, Double> linkedMap = new LinkedHashMap<>();
    linkedMap.put("pi", 3.14);
    linkedMap.put("e", 2.718);
    obj.setExplicitLinkedHashMap(linkedMap);

    obj.setDefaultHashSet(new HashSet<>(Set.of(1, 2, 3)));
    obj.setExplicitHashSet(new HashSet<>(Set.of("set1", "set2")));

    return obj;
  }
}
