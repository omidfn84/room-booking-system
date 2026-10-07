package scheduler.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small JSON reader and writer, so the web layer needs no extra library.
 *
 * Reading turns JSON text into plain Java values: Map (object), List (array),
 * String, Long or Double (number), Boolean, and null. Writing does the reverse.
 * It is deliberately strict: anything that is not valid JSON is rejected with
 * an IllegalArgumentException, which the API reports as "400 Bad Request".
 */
final class Json {

    // Stops a request like [[[[[[...]]]]]] from overflowing the call stack.
    private static final int MAX_DEPTH = 16;

    private final String text; // the JSON being read
    private int pos;           // index of the next character to read

    private Json(String text) {
        this.text = text;
    }

    // ------------------------------------------------------------------ reading


    static Object parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Invalid JSON: nothing to read.");
        }
        Json reader = new Json(text);
        reader.skipWhitespace();
        Object value = reader.readValue(0);
        reader.skipWhitespace();
        if (reader.pos != text.length()) {          // e.g. {"a":1} trailing-junk
            throw reader.error("Unexpected text after the JSON value");
        }
        return value;
    }

    /** Parses text that must be one JSON object, e.g. {"email":"a@b.ca"}. */
    static Map<String, Object> parseObject(String text) {
        Object value = parse(text);
        if (!(value instanceof Map)) {              // e.g. the body was [1,2] or "hi"
            throw new IllegalArgumentException("Request body must be a JSON object.");
        }
        @SuppressWarnings("unchecked")              // readObject only builds Map<String, Object>
        Map<String, Object> object = (Map<String, Object>) value;
        return object;
    }

    private Object readValue(int depth) {
        if (depth > MAX_DEPTH) {
            throw error("JSON is nested too deeply");
        }
        if (pos >= text.length()) {
            throw error("Unexpected end of JSON");
        }
        char c = text.charAt(pos);
        if (c == '{') return readObject(depth);
        if (c == '[') return readArray(depth);
        if (c == '"') return readString();
        if (c == 't') return readLiteral("true", Boolean.TRUE);
        if (c == 'f') return readLiteral("false", Boolean.FALSE);
        if (c == 'n') return readLiteral("null", null);
        if (c == '-' || (c >= '0' && c <= '9')) return readNumber();
        throw error("Unexpected character '" + c + "'");
    }

    private Map<String, Object> readObject(int depth) {
        Map<String, Object> object = new LinkedHashMap<>(); // keeps keys in the order they were written
        pos++;                                              // skip '{'
        skipWhitespace();
        if (peek() == '}') {                                // empty object {}
            pos++;
            return object;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("Object keys must be strings");
            }
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            object.put(key, readValue(depth + 1));
            skipWhitespace();
            char next = peek();
            pos++;
            if (next == '}') return object;                 // end of object
            if (next != ',') throw error("Expected ',' or '}'");
        }
    }

    private List<Object> readArray(int depth) {
        List<Object> array = new ArrayList<>();
        pos++;                                              // skip '['
        skipWhitespace();
        if (peek() == ']') {                                // empty array []
            pos++;
            return array;
        }
        while (true) {
            skipWhitespace();
            array.add(readValue(depth + 1));
            skipWhitespace();
            char next = peek();
            pos++;
            if (next == ']') return array;                  // end of array
            if (next != ',') throw error("Expected ',' or ']'");
        }
    }

    private String readString() {
        StringBuilder out = new StringBuilder();
        pos++;                                              // skip the opening quote
        while (true) {
            if (pos >= text.length()) {
                throw error("Unterminated string");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                return out.toString();                      // closing quote
            }
            if (c < 0x20) {
                throw error("Control characters must be escaped inside strings");
            }
            if (c != '\\') {
                out.append(c);                              // an ordinary character
                continue;
            }
            if (pos >= text.length()) {
                throw error("Unterminated escape sequence");
            }
            char escaped = text.charAt(pos++);              // the character after the backslash
            switch (escaped) {
                case '"':  out.append('"');  break;
                case '\\': out.append('\\'); break;
                case '/':  out.append('/');  break;
                case 'b':  out.append('\b'); break;
                case 'f':  out.append('\f'); break;
                case 'n':  out.append('\n'); break;
                case 'r':  out.append('\r'); break;
                case 't':  out.append('\t'); break;
                case 'u':  out.append(readUnicodeEscape()); break;
                default:   throw error("Unknown escape \\" + escaped);
            }
        }
    }

    // Reads the 4 hex digits of a backslash-u escape such as é.
    private char readUnicodeEscape() {
        if (pos + 4 > text.length()) {
            throw error("Incomplete \\u escape");
        }
        int code = 0;
        for (int i = 0; i < 4; i++) {
            int digit = Character.digit(text.charAt(pos++), 16); // -1 when it is not a hex digit
            if (digit < 0) {
                throw error("Invalid \\u escape");
            }
            code = code * 16 + digit;
        }
        return (char) code;
    }

    private Object readNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        int digitsStart = pos;
        while (pos < text.length() && isDigit(text.charAt(pos))) pos++;
        if (pos == digitsStart) {
            throw error("Invalid number");                  // a lone "-"
        }
        if (text.charAt(digitsStart) == '0' && pos - digitsStart > 1) {
            throw error("Numbers cannot have leading zeros");
        }
        boolean isInteger = true;
        if (pos < text.length() && text.charAt(pos) == '.') { // fraction part
            isInteger = false;
            pos++;
            int fractionStart = pos;
            while (pos < text.length() && isDigit(text.charAt(pos))) pos++;
            if (pos == fractionStart) throw error("Invalid number");
        }
        if (pos < text.length() && (text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) { // exponent part
            isInteger = false;
            pos++;
            if (pos < text.length() && (text.charAt(pos) == '+' || text.charAt(pos) == '-')) pos++;
            int exponentStart = pos;
            while (pos < text.length() && isDigit(text.charAt(pos))) pos++;
            if (pos == exponentStart) throw error("Invalid number");
        }
        String number = text.substring(start, pos);
        if (isInteger) {
            try {
                return Long.parseLong(number);
            } catch (NumberFormatException tooBig) {
                // more digits than a long can hold: fall through and keep it as a double
            }
        }
        return Double.parseDouble(number);
    }

    private Object readLiteral(String word, Object value) {
        if (!text.startsWith(word, pos)) {
            throw error("Unexpected text");
        }
        pos += word.length();
        return value;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void skipWhitespace() {
        while (pos < text.length()) {
            char c = text.charAt(pos);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') return;
            pos++;
        }
    }

    // The next character, or 0 at the end of the text (0 never matches a JSON token).
    private char peek() {
        return pos < text.length() ? text.charAt(pos) : '\0';
    }

    private void expect(char wanted) {
        if (peek() != wanted) {
            throw error("Expected '" + wanted + "'");
        }
        pos++;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Invalid JSON: " + message + " (at character " + pos + ").");
    }

    // ------------------------------------------------------------------ writing

    /** Turns a Map, Iterable, array, String, Number, Boolean or null into JSON text. */
    static String write(Object value) {
        StringBuilder out = new StringBuilder();
        writeValue(out, value);
        return out.toString();
    }

    private static void writeValue(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String) {
            writeString(out, (String) value);
        } else if (value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Double || value instanceof Float) {
            double number = ((Number) value).doubleValue();
            if (Double.isNaN(number) || Double.isInfinite(number)) {
                out.append("null");                         // JSON has no NaN or Infinity
            } else if (number == Math.rint(number) && Math.abs(number) < 1e15) {
                out.append((long) number);                  // 20.0 is written as 20
            } else {
                out.append(number);
            }
        } else if (value instanceof Number) {
            out.append(value);                              // Integer, Long, ...
        } else if (value instanceof Map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) out.append(',');
                first = false;
                writeString(out, String.valueOf(entry.getKey()));
                out.append(':');
                writeValue(out, entry.getValue());
            }
            out.append('}');
        } else if (value instanceof Object[]) {
            writeValue(out, java.util.Arrays.asList((Object[]) value)); // an array is written like a list
        } else if (value instanceof Iterable) {
            out.append('[');
            boolean first = true;
            for (Object item : (Iterable<?>) value) {
                if (!first) out.append(',');
                first = false;
                writeValue(out, item);
            }
            out.append(']');
        } else {
            writeString(out, value.toString());             // enums and anything else become strings
        }
    }

    private static void writeString(StringBuilder out, String s) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                default:
                    // Control characters must be escaped. '<', '>' and '&' are escaped
                    // too, so the text stays harmless even if it is ever placed in HTML.
                    if (c < 0x20 || c == '<' || c == '>' || c == '&' || c == 0x2028 || c == 0x2029) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        out.append('"');
    }
}
