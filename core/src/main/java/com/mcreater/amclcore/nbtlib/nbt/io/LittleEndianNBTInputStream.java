package com.mcreater.amclcore.nbtlib.nbt.io;

import com.mcreater.amclcore.nbtlib.common.TagType;
import com.mcreater.amclcore.nbtlib.common.io.MaxDepthIO;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.nbtlib.common.tags.EndTag;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class LittleEndianNBTInputStream implements DataInput, NBTInput, MaxDepthIO, Closeable {
    private final DataInputStream input;

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public LittleEndianNBTInputStream(InputStream in) {
        this.input = new DataInputStream(in);
    }

    public LittleEndianNBTInputStream(DataInputStream in) {
        this.input = in;
    }

    public void readFully(byte @NotNull [] b) throws IOException {
        input.readFully(b);
    }

    public void readFully(byte @NotNull [] b, int off, int len) throws IOException {
        input.readFully(b, off, len);
    }

    public int skipBytes(int n) throws IOException {
        return input.skipBytes(n);
    }

    public boolean readBoolean() throws IOException {
        return input.readBoolean();
    }

    public byte readByte() throws IOException {
        return input.readByte();
    }

    public int readUnsignedByte() throws IOException {
        return input.readUnsignedByte();
    }

    public short readShort() throws IOException {
        return Short.reverseBytes(input.readShort());
    }

    public int readUnsignedShort() throws IOException {
        return Short.toUnsignedInt(Short.reverseBytes(input.readShort()));
    }

    public char readChar() throws IOException {
        return Character.reverseBytes(input.readChar());
    }

    public int readInt() throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    public long readLong() throws IOException {
        return Long.reverseBytes(input.readLong());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(Integer.reverseBytes(input.readInt()));
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(Long.reverseBytes(input.readLong()));
    }

    @Deprecated
    public String readLine() throws IOException {
        return input.readLine();
    }

    public void close() throws IOException {
        input.close();
    }

    public String readUTF() throws IOException {
        byte[] bytes = new byte[readUnsignedShort()];
        readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public NamedTag readTag(int maxDepth) throws IOException {
        byte id = readByte();
        return new NamedTag(readUTF(), readTag(id, maxDepth));
    }

    public AbstractTag<?> readRawTag(int maxDepth) throws IOException {
        byte id = readByte();
        return readTag(id, maxDepth);
    }

    private AbstractTag<?> readTag(byte type, int maxDepth) throws IOException {
        TagType tg = TagType.search(type);
        if (tg == null) throw new IOException("unknown tag type: " + type);
        switch (tg) {
            default:
            case END_TAG:
                return EndTag.INSTANCE;
            case BYTE_TAG:
                return tg.generate(readByte());
            case SHORT_TAG:
                return tg.generate(readShort());
            case INTEGER_TAG:
                return tg.generate(readInt());
            case LONG_TAG:
                return tg.generate(readLong());
            case FLOAT_TAG:
                return tg.generate(readFloat());
            case DOUBLE_TAG:
                return tg.generate(readDouble());
            case BYTE_ARRAY_TAG:
                byte[] value = new byte[readInt()];
                readFully(value);
                return tg.generate(value);
            case STRING_TAG:
                return tg.generate(readUTF());
            case LIST_TAG:
                byte listTagType = readByte();
                TagType tgl = TagType.search(listTagType);
                if (tgl == null) throw new IOException("unknown list type: " + listTagType);

                List<AbstractTag<?>> tagList = new Vector<>();

                int length = readInt();
                if (length < 0) return tg.generate(new Vector<>());
                while (length > 0) {
                    tagList.add(readTag(listTagType, maxDepth));
                    length--;
                }
                return tg.generate(new ImmutablePair<>(tgl, tagList));
            case COMPOUND_TAG:
                Map<String, AbstractTag<?>> tagMap = new HashMap<>();
                for (int id = readByte() & 0xFF; id != 0; id = readByte() & 0xFF) {
                    String key = readUTF();
                    AbstractTag<?> element = readTag((byte) id, checkDepth(maxDepth));
                    tagMap.put(key, element);
                }
                return tg.generate(tagMap);
            case INTEGER_ARRAY_TAG:
                int lengthi = readInt();
                int[] datai = new int[lengthi];
                for (int index = 0; index < lengthi; index++) {
                    datai[index] = readInt();
                }
                return tg.generate(datai);
            case LONG_ARRAY_TAG:
                int lengthl = readInt();
                long[] datal = new long[lengthl];
                for (int index = 0; index < lengthl; index++) {
                    datal[index] = readLong();
                }
                return tg.generate(datal);
        }
    }
}
