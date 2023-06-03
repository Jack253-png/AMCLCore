package com.mcreater.amclcore.nbtlib.common.io;

import java.io.*;

public interface StringDeserializer<T> extends Deserializer<T> {
    T fromReader(Reader reader) throws IOException;

    default T fromString(String s) throws IOException {
        return fromReader(new StringReader(s));
    }

    default T fromStream(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            return fromReader(reader);
        }
    }

    default T fromFile(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return fromReader(reader);
        }
    }

    default T fromBytes(byte[] data) throws IOException {
        return fromReader(new StringReader(new String(data)));
    }
}
