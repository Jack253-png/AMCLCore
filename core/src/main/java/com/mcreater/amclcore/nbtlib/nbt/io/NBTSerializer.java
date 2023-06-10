package com.mcreater.amclcore.nbtlib.nbt.io;

import com.mcreater.amclcore.nbtlib.common.io.Serializer;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class NBTSerializer implements Serializer<NamedTag> {

    private boolean compressed, littleEndian;

    public NBTSerializer() {
        this(true);
    }

    public NBTSerializer(boolean compressed) {
        this.compressed = compressed;
    }

    public NBTSerializer(boolean compressed, boolean littleEndian) {
        this.compressed = compressed;
        this.littleEndian = littleEndian;
    }

    @Override
    public void toStream(NamedTag object, OutputStream out) throws IOException {
        NBTOutput nbtOut;
        OutputStream output;
        if (compressed) {
            output = new GZIPOutputStream(out, true);
        } else {
            output = out;
        }

        if (littleEndian) {
            nbtOut = new LittleEndianNBTOutputStream(output);
        } else {
            nbtOut = new NBTOutputStream(output);
        }
        nbtOut.writeTag(object, AbstractTag.DEFAULT_MAX_DEPTH);
        nbtOut.flush();
    }
}
