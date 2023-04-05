package com.mcreater.amclcore.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcreater.amclcore.util.adapters.UUIDAdapter;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonUtil {
    public static final Gson GSON_PARSER = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(UUID.class, UUIDAdapter.INSTANCE)
            .create();

    @SafeVarargs
    public static <T> List<T> createList(T... value) {
        return Arrays.stream(value)
                .collect(Collectors.toList());
    }

    public static NameValuePair createPair(String key, String value) {
        return new BasicNameValuePair(key, value);
    }
}
