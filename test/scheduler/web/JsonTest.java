package scheduler.web;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/** Tests for the small JSON reader/writer the web layer uses instead of a library. */
public class JsonTest {

    // ---------------------------------------------------------------- reading

    @Test
    public void parsesAFlatObject() {
        Map<String, Object> o = Json.parseObject("{\"email\":\"a@yorku.ca\",\"capacity\":12,\"ok\":true,\"none\":null}");
        assertEquals("a@yorku.ca", o.get("email"));
        assertEquals(12L, o.get("capacity"));
        assertEquals(Boolean.TRUE, o.get("ok"));
        assertTrue(o.containsKey("none"));
        assertNull(o.get("none"));
    }

    @Test
    public void parsesNestedObjectsAndArrays() {
        Map<String, Object> o = Json.parseObject(" { \"a\" : [ 1 , 2.5 , \"x\" , [ ] , { } ] , \"b\" : { \"c\" : false } } ");
        List<?> a = (List<?>) o.get("a");
        assertEquals(5, a.size());
        assertEquals(1L, a.get(0));
        assertEquals(2.5, a.get(1));
        assertEquals("x", a.get(2));
        assertEquals(Boolean.FALSE, ((Map<?, ?>) o.get("b")).get("c"));
    }

    @Test
    public void parsesNumbersOfEveryShape() {
        List<?> n = (List<?>) Json.parse("[0, -7, 3.25, 1e3, -2.5E-1, 123456789012345678901234567890]");
        assertEquals(0L, n.get(0));
        assertEquals(-7L, n.get(1));
        assertEquals(3.25, n.get(2));
        assertEquals(1000.0, n.get(3));
        assertEquals(-0.25, n.get(4));
        assertEquals(1.2345678901234568e29, (Double) n.get(5), 1e15); // too big for a long: kept as a double
    }

    @Test
    public void decodesStringEscapes() {
        Map<String, Object> o = Json.parseObject("{\"s\":\"q\\\" b\\\\ s\\/ n\\n t\\t r\\r f\\f b\\b u\\u00e9\\u4E2D\"}");
        assertEquals("q\" b\\ s/ n\n t\t r\r f\f b\b u\u00e9\u4e2d", o.get("s"));
    }

    @Test
    public void emptyObjectIsAccepted() {
        assertTrue(Json.parseObject("{}").isEmpty());
    }

    @Test
    public void rejectsAnythingThatIsNotAnObjectWhenAnObjectIsRequired() {
        for (String text : new String[] {"[1,2]", "\"hi\"", "42", "null", "true"}) {
            try {
                Json.parseObject(text);
                fail("accepted " + text);
            } catch (IllegalArgumentException expected) {
                assertEquals("Request body must be a JSON object.", expected.getMessage());
            }
        }
    }

    @Test
    public void rejectsMalformedJson() {
        String[] bad = {
            "", "   ", "{", "}", "{\"a\"}", "{\"a\":}", "{\"a\":1,}", "{a:1}", "{'a':1}", "{\"a\":1} x",
            "{\"a\":01}", "{\"a\":-}", "{\"a\":1.}", "{\"a\":1e}", "{\"a\":tru}", "{\"a\":\"unterminated}",
            "{\"a\":\"bad \\x escape\"}", "{\"a\":\"\\u12g4\"}", "{\"a\":\"\\u12\"}", "{\"a\":\"line\nbreak\"}",
            "[1 2]", "{\"a\":1 \"b\":2}", "{\"a\":\"x\\"
        };
        for (String text : bad) {
            try {
                Json.parse(text);
                fail("accepted malformed JSON: " + text);
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().startsWith("Invalid JSON"));
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNull() {
        Json.parse(null);
    }

    @Test
    public void rejectsVeryDeepNestingInsteadOfOverflowingTheStack() {
        String deep = "[".repeat(5000) + "]".repeat(5000);
        try {
            Json.parse(deep);
            fail("accepted 5000 levels of nesting");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("nested too deeply"));
        }
    }

    @Test
    public void acceptsNestingUpToTheLimit() {
        String ok = "[".repeat(16) + "]".repeat(16);
        assertNotNull(Json.parse(ok));
    }

    // ---------------------------------------------------------------- writing

    @Test
    public void writesEveryKindOfValue() {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("text", "hi");
        o.put("whole", 3);
        o.put("big", 9_000_000_000L);
        o.put("money", 20.0);        // whole doubles are written without ".0"
        o.put("half", 12.5);
        o.put("yes", true);
        o.put("nothing", null);
        o.put("list", Arrays.asList(1, "two"));
        o.put("array", new String[] {"a", "b"});
        o.put("enum", Thread.State.NEW);
        assertEquals("{\"text\":\"hi\",\"whole\":3,\"big\":9000000000,\"money\":20,\"half\":12.5,\"yes\":true,"
                + "\"nothing\":null,\"list\":[1,\"two\"],\"array\":[\"a\",\"b\"],\"enum\":\"NEW\"}", Json.write(o));
    }

    @Test
    public void writesNaNAndInfinityAsNull() {
        assertEquals("[null,null]", Json.write(Arrays.asList(Double.NaN, Double.POSITIVE_INFINITY)));
    }

    @Test
    public void escapesQuotesBackslashesAndControlCharacters() {
        assertEquals("\"a\\\"b\\\\c\\nd\\te\\rf\\u0001\"", Json.write("a\"b\\c\nd\te\rf\u0001"));
    }

    @Test
    public void escapesHtmlCharactersSoOutputIsHarmlessInsideAPage() {
        String written = Json.write("<script>alert(1)</script> & more");
        assertFalse(written.contains("<"));
        assertFalse(written.contains(">"));
        assertFalse(written.contains("&"));
        assertEquals("<script>alert(1)</script> & more", Json.parse(written)); // and it still reads back unchanged
    }

    @Test
    public void whatIsWrittenCanBeReadBack() {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("name", "Caf\u00e9 \"Central\" \\ room\n2");
        o.put("seats", 12L);
        o.put("rate", 12.75);
        o.put("tags", Arrays.asList("a", "b"));
        assertEquals(o, Json.parseObject(Json.write(o)));
    }
}
