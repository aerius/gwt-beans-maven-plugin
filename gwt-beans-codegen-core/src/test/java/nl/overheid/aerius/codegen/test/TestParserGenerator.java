package nl.overheid.aerius.codegen.test;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import nl.overheid.aerius.codegen.test.types.TestComplexCollectionType;

public class TestParserGenerator {

  @Test
  void testComplexCollectionType() throws IOException {
    final Class<?> targetClass = TestComplexCollectionType.class;
    final String expectedParserPath = "parsers/expected/TestComplexCollectionTypeParser.java";
    assertGeneratedMatchesExpected(targetClass, expectedParserPath);
  }

  private void assertGeneratedMatchesExpected(Class<?> targetClass, String expectedParserPath) {
    // Implementation of assertGeneratedMatchesExpected method
  }
}