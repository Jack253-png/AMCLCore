package com.mcreater.amclcore.util;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class IOStreamUtil {
    public static Reader toReader(File file) throws IOException {
        return new InputStreamReader(Files.newInputStream(file.toPath()));
    }

    public static Writer toWriter(File file) throws IOException {
        return new OutputStreamWriter(Files.newOutputStream(file.toPath()));
    }

    public static String readStream(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream)).lines()
                .collect(Collectors.joining("\n"));
    }

    public static InputStream tryOpenStream(URL url) {
        try {
            return url.openStream();
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.IO);
            return null;
        }
    }

    public static Reader newReader(InputStream s) {
        return new InputStreamReader(s, StandardCharsets.UTF_8);
    }
}
