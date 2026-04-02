package org.example.demo;

import java.util.*;

public final class JsonUtil {

    private JsonUtil() {}

    public static Builder obj() { return new Builder(); }

    public static final class Builder {
        private final StringBuilder sb = new StringBuilder("{");
        private boolean first = true;

        public Builder put(String key, Object value) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(key)).append("\":").append(encode(value));
            return this;
        }

        public String build() {
            return sb.append('}').toString();
        }
    }

    private static String encode(Object v) {
        if (v == null)              return "null";
        if (v instanceof Boolean)   return v.toString();
        if (v instanceof Number)    return v.toString();
        if (v instanceof String)    return '"' + escape((String) v) + '"';
        if (v instanceof Builder b) return b.build();
        if (v instanceof List<?> list) {
            StringBuilder a = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) a.append(',');
                first = false;
                a.append(encode(item));
            }
            return a.append(']').toString();
        }
        if (v instanceof Map<?, ?> map) {
            Builder b = new Builder();
            for (Map.Entry<?, ?> e : map.entrySet())
                b.put(e.getKey().toString(), e.getValue());
            return b.build();
        }
        return '"' + escape(v.toString()) + '"';
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) return new LinkedHashMap<>();
        try {
            Parser p = new Parser(json.trim());
            return p.parseObject();
        } catch (Exception e) {
            System.err.println("[JsonUtil] parse error: " + e.getMessage() + "  input=" + json);
            return new LinkedHashMap<>();
        }
    }

    private static final class Parser {
        private final String s;
        private int pos;

        Parser(String s) { this.s = s; }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWs();
            if (peek() == '}') { pos++; return map; }
            while (true) {
                skipWs();
                String key = parseString();
                skipWs(); expect(':'); skipWs();
                Object val = parseValue();
                map.put(key, val);
                skipWs();
                char c = s.charAt(pos++);
                if (c == '}') break;
                if (c != ',') throw new RuntimeException("Expected , or } at " + pos);
            }
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWs();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                skipWs();
                list.add(parseValue());
                skipWs();
                char c = s.charAt(pos++);
                if (c == ']') break;
                if (c != ',') throw new RuntimeException("Expected , or ] at " + pos);
            }
            return list;
        }

        Object parseValue() {
            skipWs();
            char c = peek();
            if (c == '"')  return parseString();
            if (c == '{')  return parseObject();
            if (c == '[')  return parseArray();
            if (c == 't')  { pos += 4; return Boolean.TRUE; }
            if (c == 'f')  { pos += 5; return Boolean.FALSE; }
            if (c == 'n')  { pos += 4; return null; }
            return parseNumber();
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') break;
                if (c == '\\') {
                    char esc = s.charAt(pos++);
                    switch (esc) {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case 'n'  -> sb.append('\n');
                        case 'r'  -> sb.append('\r');
                        case 't'  -> sb.append('\t');
                        default   -> sb.append(esc);
                    }
                } else sb.append(c);
            }
            return sb.toString();
        }

        Number parseNumber() {
            int start = pos;
            while (pos < s.length() && "-0123456789.eE+".indexOf(s.charAt(pos)) >= 0) pos++;
            String num = s.substring(start, pos);
            if (num.contains(".") || num.contains("e") || num.contains("E"))
                return Double.parseDouble(num);
            return Long.parseLong(num);
        }

        void expect(char c) {
            if (s.charAt(pos) != c)
                throw new RuntimeException("Expected '" + c + "' at " + pos + " but got '" + s.charAt(pos) + "'");
            pos++;
        }

        char peek() { return s.charAt(pos); }

        void skipWs() { while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++; }
    }

    public static String getStr(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : v.toString();
    }

    public static int getInt(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }

    public static long getLong(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }

    public static double getDbl(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0.0; }
    }

    public static boolean getBool(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        return "true".equalsIgnoreCase(v.toString());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Map<?, ?> map) return (Map<String, Object>) map;
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof List<?> list) return (List<Object>) list;
        return new ArrayList<>();
    }

    public static String ok(String dataJson) {
        return "{\"ok\":true,\"data\":" + dataJson + "}";
    }

    public static String ok() {
        return "{\"ok\":true,\"data\":null}";
    }

    public static String err(String message) {
        return "{\"ok\":false,\"data\":null,\"error\":\""
                + escape(message) + "\"}";
    }
}
