package com.mcreater.amclcore.nbtlib.snbt.io;

import com.mcreater.amclcore.nbtlib.common.io.StringDeserializer;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.Collectors;

public class SNBTDeserializer implements StringDeserializer<AbstractTag<?>> {
    @Override
    public AbstractTag<?> fromReader(Reader reader) throws IOException {
        return fromReader(reader, AbstractTag.DEFAULT_MAX_DEPTH);
    }

    public AbstractTag<?> fromReader(Reader reader, int maxDepth) throws IOException {
        BufferedReader bufferedReader;
        if (reader instanceof BufferedReader) {
            bufferedReader = (BufferedReader) reader;
        } else {
            bufferedReader = new BufferedReader(reader);
        }
        return SNBTParser.create(bufferedReader.lines().collect(Collectors.joining())).parse(maxDepth);
    }
}
