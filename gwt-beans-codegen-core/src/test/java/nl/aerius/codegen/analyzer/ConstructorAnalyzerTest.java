package nl.aerius.codegen.analyzer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.aerius.codegen.util.Logger;

/**
 * Tests for {@link ConstructorAnalyzer}, in particular the matching of source-level
 * parameter types with reflection types when generics are involved.
 */
class ConstructorAnalyzerTest {

  private static final List<String> SOURCE_ROOTS = List.of("src/test/java");

  private ConstructorAnalyzer analyzer;

  @BeforeEach
  void setUp() {
    analyzer = new ConstructorAnalyzer(SOURCE_ROOTS, new Logger() {});
  }

  @Test
  void testFindMatchingConstructorInfo_withGenericParameterTypes() {
    final Optional<ConstructorInfo> info = analyzer
        .findMatchingConstructorInfo(ConstructorAnalyzerGenericTypesFixture.class);

    assertTrue(info.isPresent(),
        "Expected constructor-based parsing to match a constructor whose parameters are List<String>, Map<String, Integer> and Set<String>");
    assertEquals(List.of("names", "counts", "tags"), info.get().getParameterNames());
    assertArrayEquals(new Class<?>[] {List.class, Map.class, Set.class},
        info.get().getConstructor().getParameterTypes());
  }

  @Test
  void testFindMatchingConstructorInfo_forRecord_usesCanonicalConstructor() {
    // Records resolve via getRecordComponents() and don't need source-file lookup -
    // construct an analyzer with no source roots to prove the record path skips it.
    final ConstructorAnalyzer reflectionOnly = new ConstructorAnalyzer(List.of(), new Logger() {});
    final Optional<ConstructorInfo> info = reflectionOnly.findMatchingConstructorInfo(RecordFixture.class);

    assertTrue(info.isPresent(), "Expected canonical record constructor to be discovered via reflection");
    assertEquals(List.of("name", "value", "tags"), info.get().getParameterNames());
    assertArrayEquals(new Class<?>[] {String.class, int.class, List.class},
        info.get().getConstructor().getParameterTypes());
  }

  /** Inline record used to assert the record-specific constructor lookup path. */
  private record RecordFixture(String name, int value, List<String> tags) {}
}
