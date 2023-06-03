package com.mcreater.amclcore.resources;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceFetcher {
    @Getter
    private static URLClassLoader CLASSLOADER = new URLClassLoader(new URL[0], ResourceFetcher.class.getClassLoader());

    public static void addRes(URL[] urls) throws IOException {
        URL[] u = CLASSLOADER.getURLs();
        CLASSLOADER.close();
        CLASSLOADER = null;
        CLASSLOADER = new URLClassLoader(Stream.concat(Arrays.stream(u), Arrays.stream(urls)).toArray(URL[]::new), ResourceFetcher.class.getClassLoader());
    }

    public static void addRes(URL url) throws IOException {
        addRes(new URL[]{url});
    }

    public static void addRes(List<URL> url) throws IOException {
        addRes(url.toArray(new URL[0]));
    }

    public static void removeRes(URL[] urls) throws IOException {
        URL[] u = CLASSLOADER.getURLs();
        List<URL> u2 = Arrays.stream(u).collect(Collectors.toList());
        u2.removeAll(Arrays.stream(urls).collect(Collectors.toList()));
        CLASSLOADER.close();
        CLASSLOADER = null;
        CLASSLOADER = new URLClassLoader(u2.toArray(new URL[0]), ResourceFetcher.class.getClassLoader());
    }

    public static void removeRes(URL url) throws IOException {
        removeRes(new URL[]{url});
    }

    public static void removeRes(List<URL> url) throws IOException {
        removeRes(url.toArray(new URL[0]));
    }

    public static InputStream get(String id, String type, String name) {
        return get(String.format("assets/%s/%s/%s", id, type, name));
    }

    public static InputStream get(String path) {
        return CLASSLOADER.getResourceAsStream(path);
    }

    public static List<URL> getFiles(String path) throws IOException {
        return Collections.list(CLASSLOADER.getResources(path));
    }
}
