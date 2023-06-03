package com.mcreater.amclcore.nbtlib.nbt.io;

import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;

import java.io.IOException;

public interface NBTInput {
    NamedTag readTag(int maxDepth) throws IOException;

    AbstractTag<?> readRawTag(int maxDepth) throws IOException;

    void close() throws IOException;
}
