package com.mcreater.amclcore.config;

import com.mcreater.amclcore.model.ConfigModel;
import com.mcreater.amclcore.util.IOStreamUtil;

import java.io.File;
import java.io.IOException;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;

public class ConfigParser {
    private ConfigParser() {}
    public static <T extends ConfigModel> T parse(File configFile, Class<T> clazz) throws IOException {
        return GSON_PARSER.fromJson(IOStreamUtil.toReader(configFile), clazz);
    }

    public static <T extends ConfigModel> void write(File configFile, T model) throws IOException {
        GSON_PARSER.toJson(model, IOStreamUtil.toWriter(configFile));
    }
}
