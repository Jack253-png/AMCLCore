package com.mcreater.amclcore.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class ResourceFetcher {
    public static InputStream get(String id, String type, String name) {
        return get(String.format("assets/%s/%s/%s", id, type, name));
    }

    public static InputStream get(String path) {
        return ResourceFetcher.class.getClassLoader().getResourceAsStream(path);
    }

    public static List<URL> getFiles(String path) throws IOException {
        return Collections.list(ResourceFetcher.class.getClassLoader().getResources("lang-index.json"));
    }
}
