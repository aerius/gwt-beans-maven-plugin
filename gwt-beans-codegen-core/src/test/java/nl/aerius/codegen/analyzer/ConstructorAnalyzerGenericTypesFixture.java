package nl.aerius.codegen.analyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fixture class for {@link ConstructorAnalyzerTest} that exercises constructor
 * parameters with generic types ({@code List<X>}, {@code Map<K, V>}, {@code Set<X>}).
 * The class is intentionally immutable (no setters) so that the analyzer is forced
 * to consider constructor-based parsing.
 */
public class ConstructorAnalyzerGenericTypesFixture {
  private final List<String> names;
  private final Map<String, Integer> counts;
  private final Set<String> tags;

  public ConstructorAnalyzerGenericTypesFixture(final List<String> names, final Map<String, Integer> counts,
      final Set<String> tags) {
    this.names = names;
    this.counts = counts;
    this.tags = tags;
  }

  public List<String> getNames() {
    return names;
  }

  public Map<String, Integer> getCounts() {
    return counts;
  }

  public Set<String> getTags() {
    return tags;
  }
}
