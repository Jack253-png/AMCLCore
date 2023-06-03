package com.mcreater.amclcore.nbtlib.common.io;

import java.io.*;
import java.nio.file.Files;

public interface Serializer<T> {
    void toStream(T object, OutputStream out) throws IOException;

    default void toFile(T object, File file) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            toStream(object, bos);
        }
    }

    default byte[] toBytes(T object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        toStream(object, bos);
        bos.close();
        return bos.toByteArray();
    }
}
