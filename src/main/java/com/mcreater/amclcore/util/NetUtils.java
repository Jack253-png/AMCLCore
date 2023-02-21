package com.mcreater.amclcore.util;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class NetUtils {
    public static Pair<String, String> parseToPair(String url) {
        int index = url.indexOf("/");
        String path = url.substring(index);
        String host = url.substring(0, index);
        return new ImmutablePair<>(host, path);
    }
}
