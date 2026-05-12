package nl.aerius.codegen.test.types;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable test type whose constructor parameters cover the generic-collection
 * and primitive-array cases that exercise:
 * - ConstructorAnalyzer matching List&lt;X&gt;/Map&lt;K,V&gt;/Set&lt;X&gt; source
 *   types against raw reflection types (stripGenerics).
 * - CollectionFieldParser, MapFieldParser, and PrimitiveArrayFieldParser using
 *   the field name for their result variable (variableName overload) so sibling
 *   constructor params don't collide on level-based names.
 */
public class TestConstructorWithGenericsType {
  private final List<String> tags;
  private final Map<String, Integer> counts;
  private final Set<String> labels;
  private final int[] sizes;
  private final String[] aliases;

  public TestConstructorWithGenericsType(final List<String> tags, final Map<String, Integer> counts,
      final Set<String> labels, final int[] sizes, final String[] aliases) {
    this.tags = tags;
    this.counts = counts;
    this.labels = labels;
    this.sizes = sizes;
    this.aliases = aliases;
  }

  public List<String> getTags() {
    return tags;
  }

  public Map<String, Integer> getCounts() {
    return counts;
  }

  public Set<String> getLabels() {
    return labels;
  }

  public int[] getSizes() {
    return sizes;
  }

  public String[] getAliases() {
    return aliases;
  }

  public static TestConstructorWithGenericsType createFullObject() {
    final Map<String, Integer> counts = new LinkedHashMap<>();
    counts.put("alpha", 1);
    counts.put("beta", 2);
    final Set<String> labels = new HashSet<>();
    labels.add("first");
    labels.add("second");
    return new TestConstructorWithGenericsType(
        List.of("a", "b", "c"),
        counts,
        labels,
        new int[] {10, 20, 30},
        new String[] {"x", "y"});
  }

  public static TestConstructorWithGenericsType createNullObject() {
    return new TestConstructorWithGenericsType(null, null, null, null, null);
  }
}
