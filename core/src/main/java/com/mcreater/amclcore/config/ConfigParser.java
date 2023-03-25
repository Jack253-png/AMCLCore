package com.mcreater.amclcore.config;

import com.mcreater.amclcore.annotations.ConfigModel;
import com.mcreater.amclcore.util.IOStreamUtil;

import java.io.File;
import java.io.IOException;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;

public class ConfigParser {
    private ConfigParser() {
    }

    public static <T> T parse(File configFile, Class<T> clazz) throws IOException {
        if (clazz.getAnnotation(ConfigModel.class) != null)
            return GSON_PARSER.fromJson(IOStreamUtil.toReader(configFile), clazz);
        else throw new IOException("Not a config model");
    }

    public static <T> void write(File configFile, T model) throws IOException {
        GSON_PARSER.toJson(model, IOStreamUtil.toWriter(configFile));
    }
}
