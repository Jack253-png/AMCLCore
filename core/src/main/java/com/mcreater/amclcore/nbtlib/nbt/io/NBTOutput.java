package com.mcreater.amclcore.nbtlib.nbt.io;

import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;

import java.io.IOException;

public interface NBTOutput {
    void writeTag(NamedTag tag, int maxDepth) throws IOException;

    void writeTag(AbstractTag<?> tag, int maxDepth) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;
}
