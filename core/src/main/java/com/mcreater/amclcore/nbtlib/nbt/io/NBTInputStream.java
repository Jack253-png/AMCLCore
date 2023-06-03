package com.mcreater.amclcore.nbtlib.nbt.io;

import com.mcreater.amclcore.nbtlib.common.TagType;
import com.mcreater.amclcore.nbtlib.common.io.MaxDepthIO;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.nbtlib.common.tags.EndTag;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class NBTInputStream extends DataInputStream implements NBTInput, MaxDepthIO {
    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public NBTInputStream(@NotNull InputStream in) {
        super(in);
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
