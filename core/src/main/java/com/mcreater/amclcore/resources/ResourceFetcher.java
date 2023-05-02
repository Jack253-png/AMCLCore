package com.mcreater.amclcore.resources;

import java.io.InputStream;

public class ResourceFetcher {
    public static InputStream get(String id, String type, String name) {
        return get(String.format("assets/%s/%s/%s", id, type, name));
    }

    public static InputStream get(String path) {
        System.out.println(path);
        return ResourceFetcher.class.getClassLoader().getResourceAsStream(path);
    }
}
