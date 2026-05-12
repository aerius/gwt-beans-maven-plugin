package nl.aerius.codegen.generator.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

/**
 * Tests the canHandle dispatch guards on CustomObjectFieldParser, in particular the
 * JDK-Map exclusion that prevents raw java.util Map types from routing to a
 * non-existent generated parser, while still letting user Map subclasses fall through
 * so they can be picked up by a custom parser.
 */
class CustomObjectFieldParserTest {

  private final CustomObjectFieldParser parser = new CustomObjectFieldParser(new HashMap<>());

  @Test
  void canHandleRejectsRawJdkMapTypes() {
    assertFalse(parser.canHandle(Map.class), "raw Map should not route to CustomObjectFieldParser");
    assertFalse(parser.canHandle(HashMap.class), "raw HashMap should not route to CustomObjectFieldParser");
    assertFalse(parser.canHandle(LinkedHashMap.class), "raw LinkedHashMap should not route to CustomObjectFieldParser");
    assertFalse(parser.canHandle(TreeMap.class), "raw TreeMap should not route to CustomObjectFieldParser");
  }

  @Test
  void canHandleAcceptsUserMapSubclass() {
    assertTrue(parser.canHandle(UserMapSubclass.class),
        "user-defined Map subclass should fall through so a custom parser can handle it by simple name");
  }

  /** User-defined Map subclass - by extending HashMap from a non-java.* package, it should not be excluded. */
  private static final class UserMapSubclass extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;
  }
}
