package com.mcreater.amclcore.util;

import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.util.EntityUtils;

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

    public static String readFrom(MinecraftMirroredResourceURL url) throws URISyntaxException, IOException {
        return EntityUtils.toString(
                HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                        .uriScheme(url.toDownloadFormatWithMirror().getLeft())
                        .uri(url.toDownloadFormatWithMirror().getRight())
                        .send()
        );
    }
}
