package com.mcreater.amclcore.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

public class IOStreamUtil {
    public static Reader toReader(File file) throws IOException {
        return new InputStreamReader(Files.newInputStream(file.toPath()));
    }

    public static Writer toWriter(File file) throws IOException {
        return new OutputStreamWriter(Files.newOutputStream(file.toPath()));
    }
}
