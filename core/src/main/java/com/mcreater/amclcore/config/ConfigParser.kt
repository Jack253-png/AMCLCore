package com.mcreater.amclcore.config

import com.mcreater.amclcore.annotations.ConfigModel
import com.mcreater.amclcore.util.IOStreamUtil
import com.mcreater.amclcore.util.JsonUtil
import java.io.File
import java.io.IOException


class ConfigParser {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun <T> parse(configFile: File?, clazz: Class<T>): T {
            return if (clazz.getAnnotation(ConfigModel::class.java) != null) JsonUtil.GSON_PARSER.fromJson(
                IOStreamUtil.toReader(
                    configFile
                ), clazz
            ) else throw IOException("Not a config model")
        }

        @JvmStatic
        @Throws(IOException::class)
        fun <T> write(configFile: File?, model: T) {
            JsonUtil.GSON_PARSER.toJson(model, IOStreamUtil.toWriter(configFile))
        }
    }
}

