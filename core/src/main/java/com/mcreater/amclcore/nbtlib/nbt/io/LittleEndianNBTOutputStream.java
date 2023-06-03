package com.mcreater.amclcore.nbtlib.nbt.io;

import com.mcreater.amclcore.nbtlib.common.TagType;
import com.mcreater.amclcore.nbtlib.common.io.MaxDepthIO;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.nbtlib.common.tags.EndTag;
import com.mcreater.amclcore.nbtlib.common.tags.ListTag;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import static com.mcreater.amclcore.util.ArrayUtils.debox;

public class LittleEndianNBTOutputStream implements DataOutput, NBTOutput, MaxDepthIO, Closeable {
    private final DataOutputStream output;

    public LittleEndianNBTOutputStream(OutputStream out) {
        output = new DataOutputStream(out);
    }

    public LittleEndianNBTOutputStream(DataOutputStream out) {
        output = out;
    }

    public void writeTag(NamedTag tag, int maxDepth) throws IOException {
        byte id = Objects.requireNonNull(TagType.search(tag.getTag().getClass())).getId();
        writeByte(id);
        if (id != 0) {
            writeUTF(tag.getName() == null ? "" : tag.getName());
        }
        writeRawTag(tag.getTag(), maxDepth);
    }

    public void writeTag(AbstractTag<?> tag, int maxDepth) throws IOException {
        byte id = Objects.requireNonNull(TagType.search(tag.getClass())).getId();
        writeByte(id);
        if (id != 0) {
            writeUTF("");
        }
        writeRawTag(tag, maxDepth);
    }

    public void writeRawTag(AbstractTag<?> tag, int maxDepth) throws IOException {
        TagType tg = TagType.search(tag.getClass());
        if (tg == null) throw new IOException("unknown tag type: " + tag.getClass());
        switch (tg) {
            default:
            case END_TAG:
                break;
            case BYTE_TAG:
                writeByte(tag.toNumberTag().asByte());
                break;
            case SHORT_TAG:
                writeShort(tag.toNumberTag().asShort());
                break;
            case INTEGER_TAG:
                writeInt(tag.toNumberTag().asInteger());
                break;
            case LONG_TAG:
                writeLong(tag.toNumberTag().asLong());
                break;
            case FLOAT_TAG:
                writeFloat(tag.toNumberTag().asFloat());
                break;
            case DOUBLE_TAG:
                writeDouble(tag.toNumberTag().asDouble());
                break;
            case BYTE_ARRAY_TAG:
                writeInt(tag.toByteArrayTag().size());
                write(debox(tag.toByteArrayTag().getValue()));
                break;
            case STRING_TAG:
                writeUTF(tag.toStringTag().getValue());
                break;
            case LIST_TAG:
                write(Objects.requireNonNull(TagType.search(tag.toListTag().getTypeClass())).getId());
                writeInt(tag.toListTag().size());
                for (AbstractTag<?> t : ((ListTag<?>) tag)) {
                    writeRawTag(t, checkDepth(maxDepth));
                }
                break;
            case COMPOUND_TAG:
                for (Map.Entry<String, AbstractTag<?>> entry : tag.toCompoundTag()) {
                    if (entry.getValue() instanceof EndTag) throw new IOException("end tag not allowed");
                    writeByte(Objects.requireNonNull(TagType.search(entry.getValue().getClass())).getId());
                    writeUTF(entry.getKey());
                    writeRawTag(entry.getValue(), checkDepth(maxDepth));
                }
                writeByte(0);
                break;
            case INTEGER_ARRAY_TAG:
                writeInt(tag.toIntegerArrayTag().size());
                for (int i : tag.toIntegerArrayTag().getValue()) {
                    writeInt(i);
                }
                break;
            case LONG_ARRAY_TAG:
                writeInt(tag.toLongArrayTag().size());
                for (long l : tag.toLongArrayTag().getValue()) {
                    writeLong(l);
                }
                break;
        }
    }

    public void close() throws IOException {
        output.close();
    }

    public void flush() throws IOException {
        output.flush();
    }

    public void write(int b) throws IOException {
        output.write(b);
    }

    public void write(byte[] b) throws IOException {
        output.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
    }

    public void writeBoolean(boolean v) throws IOException {
        output.writeBoolean(v);
    }

    public void writeByte(int v) throws IOException {
        output.writeByte(v);
    }

    public void writeShort(int v) throws IOException {
        output.writeShort(Short.reverseBytes((short) v));
    }

    public void writeChar(int v) throws IOException {
        output.writeChar(Character.reverseBytes((char) v));
    }

    public void writeInt(int v) throws IOException {
        output.writeInt(Integer.reverseBytes(v));
    }

    public void writeLong(long v) throws IOException {
        output.writeLong(Long.reverseBytes(v));
    }

    public void writeFloat(float v) throws IOException {
        output.writeInt(Integer.reverseBytes(Float.floatToIntBits(v)));
    }

    public void writeDouble(double v) throws IOException {
        output.writeLong(Long.reverseBytes(Double.doubleToLongBits(v)));
    }

    public void writeBytes(String s) throws IOException {
        output.writeBytes(s);
    }

    public void writeChars(String s) throws IOException {
        output.writeChars(s);
    }

    public void writeUTF(String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeShort(bytes.length);
        write(bytes);
    }
}
