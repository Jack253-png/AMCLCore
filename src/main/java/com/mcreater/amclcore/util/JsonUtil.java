package com.mcreater.amclcore.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
    public static final Gson GSON_PARSER = new GsonBuilder()
            .setPrettyPrinting()
            .create();
}
