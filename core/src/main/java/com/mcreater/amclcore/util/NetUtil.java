package com.mcreater.amclcore.util;

import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class NetUtil {
    public static Pair<String, String> parseToPair(String url) {
        int index = url.indexOf("/");
        String path = url.substring(index);
        String host = url.substring(0, index);
        return new ImmutablePair<>(host, path);
    }

    public static String buildScopeString(String delimiter, List<String> s) {
        return String.join(delimiter, s);
    }

    public static String buildScopeString(String delimiter, String... s) {
        return buildScopeString(delimiter, Arrays.asList(s));
    }

    public static <T> T readFrom(MinecraftMirroredResourceURL url, Class<T> clazz) throws URISyntaxException, IOException {
        return HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uriScheme(url.toDownloadFormatWithMirror().getLeft())
                .uri(url.toDownloadFormatWithMirror().getRight())
                .sendAndReadJson(clazz);
    }
}
