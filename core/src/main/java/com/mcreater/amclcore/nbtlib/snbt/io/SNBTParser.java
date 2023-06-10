package com.mcreater.amclcore.nbtlib.snbt.io;

import com.mcreater.amclcore.nbtlib.common.TagType;
import com.mcreater.amclcore.nbtlib.common.io.MaxDepthIO;
import com.mcreater.amclcore.nbtlib.common.tags.*;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

public final class SNBTParser implements MaxDepthIO {

    private static final Pattern
            FLOAT_LITERAL_PATTERN = Pattern.compile("^[-+]?(?:\\d+\\.?|\\d*\\.\\d+)(?:e[-+]?\\d+)?f$", Pattern.CASE_INSENSITIVE),
            DOUBLE_LITERAL_PATTERN = Pattern.compile("^[-+]?(?:\\d+\\.?|\\d*\\.\\d+)(?:e[-+]?\\d+)?d$", Pattern.CASE_INSENSITIVE),
            DOUBLE_LITERAL_NO_SUFFIX_PATTERN = Pattern.compile("^[-+]?(?:\\d+\\.|\\d*\\.\\d+)(?:e[-+]?\\d+)?$", Pattern.CASE_INSENSITIVE),
            BYTE_LITERAL_PATTERN = Pattern.compile("^[-+]?\\d+b$", Pattern.CASE_INSENSITIVE),
            SHORT_LITERAL_PATTERN = Pattern.compile("^[-+]?\\d+s$", Pattern.CASE_INSENSITIVE),
            INT_LITERAL_PATTERN = Pattern.compile("^[-+]?\\d+$", Pattern.CASE_INSENSITIVE),
            LONG_LITERAL_PATTERN = Pattern.compile("^[-+]?\\d+l$", Pattern.CASE_INSENSITIVE),
            NUMBER_PATTERN = Pattern.compile("^[-+]?\\d+$");

    private final StringPointer ptr;

    private SNBTParser(String string) {
        this.ptr = new StringPointer(string);
    }

    public static SNBTParser create(String string) {
        return new SNBTParser(string);
    }

    public AbstractTag<?> parse(int maxDepth, boolean lenient) throws ParseException {
        AbstractTag<?> tag = parseAnything(maxDepth);
        if (!lenient) {
            ptr.skipWhitespace();
            if (ptr.hasNext()) {
                throw ptr.parseException("invalid characters after end of snbt");
            }
        }
        return tag;
    }

    public AbstractTag<?> parse(int maxDepth) throws ParseException {
        return parse(maxDepth, false);
    }

    public AbstractTag<?> parse() throws ParseException {
        return parse(AbstractTag.DEFAULT_MAX_DEPTH, false);
    }

    public int getReadChars() {
        return ptr.getIndex() + 1;
    }

    private AbstractTag<?> parseAnything(int maxDepth) throws ParseException {
        ptr.skipWhitespace();
        switch (ptr.currentChar()) {
            case '{':
                return parseCompoundTag(maxDepth);
            case '[':
                if (ptr.hasCharsLeft(2) && ptr.lookAhead(1) != '"' && ptr.lookAhead(2) == ';') {
                    return parseNumArray();
                }
                return parseListTag(maxDepth);
        }
        return parseStringOrLiteral();
    }

    private AbstractTag<?> parseStringOrLiteral() throws ParseException {
        ptr.skipWhitespace();
        if (ptr.currentChar() == '"') {
            return TagType.STRING_TAG.generate(ptr.parseQuotedString());
        }
        String s = ptr.parseSimpleString();
        if (s.isEmpty()) {
            throw new ParseException("expected non empty value");
        }
        if (FLOAT_LITERAL_PATTERN.matcher(s).matches()) {
            return TagType.FLOAT_TAG.generate(Float.parseFloat(s.substring(0, s.length() - 1)));
        } else if (BYTE_LITERAL_PATTERN.matcher(s).matches()) {
            try {
                return TagType.BYTE_TAG.generate(Byte.parseByte(s.substring(0, s.length() - 1)));
            } catch (NumberFormatException ex) {
                throw ptr.parseException("byte not in range: \"" + s.substring(0, s.length() - 1) + "\"");
            }
        } else if (SHORT_LITERAL_PATTERN.matcher(s).matches()) {
            try {
                return TagType.SHORT_TAG.generate(Short.parseShort(s.substring(0, s.length() - 1)));
            } catch (NumberFormatException ex) {
                throw ptr.parseException("short not in range: \"" + s.substring(0, s.length() - 1) + "\"");
            }
        } else if (LONG_LITERAL_PATTERN.matcher(s).matches()) {
            try {
                return TagType.LONG_TAG.generate(Long.parseLong(s.substring(0, s.length() - 1)));
            } catch (NumberFormatException ex) {
                throw ptr.parseException("long not in range: \"" + s.substring(0, s.length() - 1) + "\"");
            }
        } else if (INT_LITERAL_PATTERN.matcher(s).matches()) {
            try {
                return TagType.INTEGER_TAG.generate(Integer.parseInt(s));
            } catch (NumberFormatException ex) {
                throw ptr.parseException("int not in range: \"" + s.substring(0, s.length() - 1) + "\"");
            }
        } else if (DOUBLE_LITERAL_PATTERN.matcher(s).matches()) {
            return TagType.DOUBLE_TAG.generate(Double.parseDouble(s.substring(0, s.length() - 1)));
        } else if (DOUBLE_LITERAL_NO_SUFFIX_PATTERN.matcher(s).matches()) {
            return TagType.DOUBLE_TAG.generate(Double.parseDouble(s));
        } else if ("true".equalsIgnoreCase(s)) {
            return TagType.BYTE_TAG.generate((byte) 1);
        } else if ("false".equalsIgnoreCase(s)) {
            return TagType.BYTE_TAG.generate((byte) 0);
        }
        return TagType.STRING_TAG.generate(s);
    }

    private CompoundTag parseCompoundTag(int maxDepth) throws ParseException {
        ptr.expectChar('{');

        CompoundTag compoundTag = TagType.COMPOUND_TAG.generate(new HashMap<String, AbstractTag<?>>()).toCompoundTag();

        ptr.skipWhitespace();
        while (ptr.hasNext() && ptr.currentChar() != '}') {
            ptr.skipWhitespace();
            String key = ptr.currentChar() == '"' ? ptr.parseQuotedString() : ptr.parseSimpleString();
            if (key.isEmpty()) {
                throw new ParseException("empty keys are not allowed");
            }
            ptr.expectChar(':');

            compoundTag.put(key, parseAnything(checkDepth(maxDepth)));

            if (!ptr.nextArrayElement()) {
                break;
            }
        }
        ptr.expectChar('}');
        return compoundTag;
    }

    private ListTag<?> parseListTag(int maxDepth) throws ParseException {
        ptr.expectChar('[');
        ptr.skipWhitespace();
        List<AbstractTag<?>> list = new Vector<>();
        Class<?> clazz = null;
        while (ptr.currentChar() != ']') {
            AbstractTag<?> element = parseAnything(checkDepth(maxDepth));
            if (clazz != null && element.getClass() != clazz) {
                throw new IllegalArgumentException();
            }
            clazz = element.getClass();
            try {
                list.add(element);
            } catch (IllegalArgumentException ex) {
                throw ptr.parseException(ex.getMessage());
            }
            if (!ptr.nextArrayElement()) {
                break;
            }
        }
        ptr.expectChar(']');
        if (clazz == null) clazz = EndTag.class;
        return TagType.LIST_TAG.generate(
                new ImmutablePair<>(
                        TagType.search(clazz),
                        list
                )
        ).toListTag();
    }

    private ArrayTag<?> parseNumArray() throws ParseException {
        ptr.expectChar('[');
        char arrayType = ptr.next();
        ptr.expectChar(';');
        ptr.skipWhitespace();
        switch (arrayType) {
            case 'B':
                return parseByteArrayTag();
            case 'I':
                return parseIntArrayTag();
            case 'L':
                return parseLongArrayTag();
        }
        throw new ParseException("invalid array type '" + arrayType + "'");
    }

    private ByteArrayTag parseByteArrayTag() throws ParseException {
        List<Byte> byteList = new ArrayList<>();
        while (ptr.currentChar() != ']') {
            String s = ptr.parseSimpleString();
            ptr.skipWhitespace();
            if (NUMBER_PATTERN.matcher(s).matches()) {
                try {
                    byteList.add(Byte.parseByte(s));
                } catch (NumberFormatException ex) {
                    throw ptr.parseException("byte not in range: \"" + s + "\"");
                }
            } else {
                throw ptr.parseException("invalid byte in ByteArrayTag: \"" + s + "\"");
            }
            if (!ptr.nextArrayElement()) {
                break;
            }
        }
        ptr.expectChar(']');
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            bytes[i] = byteList.get(i);
        }
        return TagType.BYTE_ARRAY_TAG.generate(bytes).toByteArrayTag();
    }

    private IntegerArrayTag parseIntArrayTag() throws ParseException {
        List<Integer> intList = new ArrayList<>();
        while (ptr.currentChar() != ']') {
            String s = ptr.parseSimpleString();
            ptr.skipWhitespace();
            if (NUMBER_PATTERN.matcher(s).matches()) {
                try {
                    intList.add(Integer.parseInt(s));
                } catch (NumberFormatException ex) {
                    throw ptr.parseException("int not in range: \"" + s + "\"");
                }
            } else {
                throw ptr.parseException("invalid int in IntArrayTag: \"" + s + "\"");
            }
            if (!ptr.nextArrayElement()) {
                break;
            }
        }
        ptr.expectChar(']');
        return TagType.INTEGER_ARRAY_TAG.generate(intList.stream().mapToInt(i -> i).toArray()).toIntegerArrayTag();
    }

    private LongArrayTag parseLongArrayTag() throws ParseException {
        List<Long> longList = new ArrayList<>();
        while (ptr.currentChar() != ']') {
            String s = ptr.parseSimpleString();
            ptr.skipWhitespace();
            if (NUMBER_PATTERN.matcher(s).matches()) {
                try {
                    longList.add(Long.parseLong(s));
                } catch (NumberFormatException ex) {
                    throw ptr.parseException("long not in range: \"" + s + "\"");
                }
            } else {
                throw ptr.parseException("invalid long in LongArrayTag: \"" + s + "\"");
            }
            if (!ptr.nextArrayElement()) {
                break;
            }
        }
        ptr.expectChar(']');
        return TagType.LONG_ARRAY_TAG.generate(longList.stream().mapToLong(l -> l).toArray()).toLongArrayTag();
    }
}