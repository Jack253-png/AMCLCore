package com.mcreater.amclcore.nbtlib.snbt.io;

import com.mcreater.amclcore.nbtlib.common.io.StringSerializer;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;

import java.io.IOException;
import java.io.Writer;

public class SNBTSerializer implements StringSerializer<AbstractTag<?>> {
    public void toWriter(AbstractTag<?> object, Writer writer) throws IOException {
        SNBTWriter.write(object, writer);
    }

    public void toWriter(AbstractTag<?> object, Writer writer, int maxDepth) throws IOException {
        SNBTWriter.write(object, writer, maxDepth);
    }
}
