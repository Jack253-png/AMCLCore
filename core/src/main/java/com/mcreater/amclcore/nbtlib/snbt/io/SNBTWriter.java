package com.mcreater.amclcore.nbtlib.snbt.io;

import com.mcreater.amclcore.nbtlib.common.TagType;
import com.mcreater.amclcore.nbtlib.common.io.MaxDepthIO;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SNBTWriter implements MaxDepthIO {
    private static final Pattern NON_QUOTE_PATTERN = Pattern.compile("[a-zA-Z_.+\\-]+");
    private Writer writer;

    public static void write(AbstractTag<?> tag, Writer writer, int maxDepth) throws IOException {
        new SNBTWriter(writer).writeAnything(tag, maxDepth);
    }

    public static void write(AbstractTag<?> tag, Writer writer) throws IOException {
        write(tag, writer, AbstractTag.DEFAULT_MAX_DEPTH);
    }

    private void writeAnything(AbstractTag<?> tag, int maxDepth) throws IOException {
        switch (Objects.requireNonNull(TagType.search(tag.getClass()))) {
            case END_TAG:
                break;
            case BYTE_TAG:
                writer.append(Byte.toString(tag.toNumberTag().asByte())).append("b");
                break;
            case SHORT_TAG:
                writer.append(Short.toString(tag.toNumberTag().asShort())).append("s");
                break;
            case INTEGER_TAG:
                writer.write(Integer.toString(tag.toNumberTag().asInteger()));
                break;
            case LONG_TAG:
                writer.append(Long.toString(tag.toNumberTag().asLong())).append("l");
                break;
            case FLOAT_TAG:
                writer.append(Float.toString(tag.toNumberTag().asLong())).append("f");
                break;
            case DOUBLE_TAG:
                writer.append(Double.toString(tag.toNumberTag().asDouble())).append("d");
                break;
            case BYTE_ARRAY_TAG:
                writeArray(tag.toByteArrayTag().getValue(), "B");
                break;
            case STRING_TAG:
                writer.write(escapeString(tag.toStringTag().getValue()));
                break;
            case LIST_TAG:
                writer.write('[');
                for (int i = 0; i < tag.toListTag().size(); i++) {
                    writer.write(i == 0 ? "" : ",");
                    writeAnything(tag.toListTag().get(i), checkDepth(maxDepth));
                }
                writer.write(']');
                break;
            case COMPOUND_TAG:
                writer.write('{');
                boolean first = true;
                for (Map.Entry<String, AbstractTag<?>> entry : tag.toCompoundTag()) {
                    writer.write(first ? "" : ",");
                    writer.append(escapeString(entry.getKey())).write(':');
                    writeAnything(entry.getValue(), checkDepth(maxDepth));
                    first = false;
                }
                writer.write('}');
                break;
            case INTEGER_ARRAY_TAG:
                writeArray(tag.toIntegerArrayTag().getValue(), "I");
                break;
            case LONG_ARRAY_TAG:
                writeArray(tag.toLongArrayTag().getValue(), "L");
                break;
            default:
                throw new IOException("invaild tag: " + tag.getClass().getName());
        }
    }

    private void writeArray(Object array, String prefix) throws IOException {
        if (!array.getClass().isArray()) throw new RuntimeException("not a array");
        writer.append('[').append(prefix).write(";");
        for (int i = 0; i < Array.getLength(array); i++) {
            writer.append(i == 0 ? "" : ",").write(Array.get(array, i).toString());
        }
        writer.write(']');
    }

    private static String escapeString(String s) {
        if (!NON_QUOTE_PATTERN.matcher(s).matches()) {
            StringBuilder sb = new StringBuilder();
            sb.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\\' || c == '"') {
                    sb.append('\\');
                }
                sb.append(c);
            }
            sb.append('"');
            return sb.toString();
        }
        return s;
    }
}
