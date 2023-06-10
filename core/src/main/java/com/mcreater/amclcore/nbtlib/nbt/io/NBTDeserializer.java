package com.mcreater.amclcore.nbtlib.nbt.io;

import com.mcreater.amclcore.nbtlib.common.io.Deserializer;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class NBTDeserializer implements Deserializer<NamedTag> {

    private boolean compressed, littleEndian;

    public NBTDeserializer() {
        this(true);
    }

    public NBTDeserializer(boolean compressed) {
        this.compressed = compressed;
    }

    public NBTDeserializer(boolean compressed, boolean littleEndian) {
        this.compressed = compressed;
        this.littleEndian = littleEndian;
    }

    @Override
    public NamedTag fromStream(InputStream stream) throws IOException {
        NBTInput nbtIn;
        InputStream input;
        if (compressed) {
            input = new GZIPInputStream(stream);
        } else {
            input = stream;
        }

        if (littleEndian) {
            nbtIn = new LittleEndianNBTInputStream(input);
        } else {
            nbtIn = new NBTInputStream(input);
        }
        return nbtIn.readTag(AbstractTag.DEFAULT_MAX_DEPTH);
    }
}
