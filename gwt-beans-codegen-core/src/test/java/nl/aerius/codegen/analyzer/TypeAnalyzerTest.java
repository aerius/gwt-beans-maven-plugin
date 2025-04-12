package nl.aerius.codegen.analyzer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.palantir.javapoet.ClassName;

class TypeAnalyzerTest {
  private TypeAnalyzer analyzer;

  @BeforeEach
  void setUp() {
    analyzer = new TypeAnalyzer();
  }

  @Test
  void testBasicTypeAnalysis() {
    Set<ClassName> types = analyzer.analyzeClass(TestClass.class.getName());

    assertTrue(types.contains(ClassName.get("nl.aerius.codegen.analyzer", "TypeAnalyzerTest_TestClass")));
    assertTrue(types.contains(ClassName.get("nl.aerius.codegen.analyzer", "TypeAnalyzerTest_NestedClass")));
  }

  @Test
  void testUnsupportedTypeDetection() {
    assertThrows(UnsupportedTypeException.class, () -> {
      analyzer.analyzeClass(UnsupportedTypeTestClass.class.getName());
    });
  }

  @Test
  void testCustomParserTypeHandling() {
    analyzer.setCustomParserTypes(Set.of("CustomParserType"));
    Set<ClassName> types = analyzer.analyzeClass(CustomParserTestClass.class.getName());
    System.out.println("Discovered types with custom parser: " + types);

    assertFalse(types.contains(ClassName.get("nl.aerius.codegen.analyzer", "TypeAnalyzerTest_CustomParserType")));
    assertTrue(types.contains(ClassName.get("nl.aerius.codegen.analyzer", "TypeAnalyzerTest_CustomParserTestClass")));
  }

  @Test
  void testNestedCollectionTypes() {
    Set<ClassName> types = analyzer.analyzeClass(NestedCollectionTestClass.class.getName());
    System.out.println("Discovered nested collection types: " + types);

    assertTrue(
        types.contains(ClassName.get("nl.aerius.codegen.analyzer", "TypeAnalyzerTest_NestedCollectionTestClass")));
    assertTrue(types.contains(ClassName.get("nl.aerius.codegen.analyzer", "TypeAnalyzerTest_DeepNestedClass")));
  }

  // Test classes
  private static class TestClass {
    private String primitiveField;
    private NestedClass nestedField;
  }

  private static class NestedClass {
    private int primitiveInt;
  }

  private static class UnsupportedTypeTestClass {
    private LocalDate unsupportedField;
  }

  private static class CustomParserType {
    private String field;
  }

  private static class CustomParserTestClass {
    private CustomParserType customParserField;
    private String regularField;
  }

  private static class DeepNestedClass {
    private String field;
  }

  private static class NestedCollectionTestClass {
    private List<Map<String, DeepNestedClass>> nestedCollection;
  }

  // Test class for complex nested structures with primitive types and enums
  private enum TestEnum {
    A, B, C
  }

  private static class ComplexNestedTestClass {
    private Map<TestEnum, Map<Integer, Map<Integer, String>>> complexNestedMap;
  }
}