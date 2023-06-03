package com.mcreater.amclcore.nbtlib.nbt.io;

import com.mcreater.amclcore.nbtlib.common.TagType;
import com.mcreater.amclcore.nbtlib.common.io.MaxDepthIO;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.nbtlib.common.tags.EndTag;
import com.mcreater.amclcore.nbtlib.common.tags.ListTag;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

import static com.mcreater.amclcore.util.ArrayUtils.debox;

public class NBTOutputStream extends DataOutputStream implements NBTOutput, MaxDepthIO {

    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream. The counter {@code written} is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     * @see FilterOutputStream#out
     */
    public NBTOutputStream(OutputStream out) {
        super(out);
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
}
