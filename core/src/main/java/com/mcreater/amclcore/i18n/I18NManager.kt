package com.mcreater.amclcore.i18n

import com.google.gson.reflect.TypeToken
import com.mcreater.amclcore.exceptions.report.ExceptionReporter
import com.mcreater.amclcore.model.i18n.LangIndexModel
import com.mcreater.amclcore.resources.ResourceFetcher
import com.mcreater.amclcore.util.IOStreamUtil
import com.mcreater.amclcore.util.JsonUtil
import org.apache.commons.lang3.tuple.ImmutablePair
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream


class I18NManager {
    companion object {
        @JvmStatic
        private var parsedIndexes: List<LangIndexModel> = Vector()

        @JvmStatic
        private val localeRemap = Stream.of(
            Locale.SIMPLIFIED_CHINESE,
            Locale.TRADITIONAL_CHINESE,
            Locale.FRANCE,
            Locale.GERMANY,
            Locale.ITALY,
            Locale.JAPAN,
            Locale.KOREA,
            Locale.UK,
            Locale.US,
            Locale.CANADA,
            Locale.CANADA_FRENCH
        )
            .collect(
                Collectors.toMap(
                    { it },
                    { parseI18N(it) })
            )

        @JvmStatic
        private val transitionMap: MutableMap<Locale?, MutableMap<String, Any>> = HashMap()

        @JvmStatic
        private val transitionFiles: MutableMap<Locale?, MutableList<String>> = HashMap()

        @JvmStatic
        private val packNames: MutableList<Text> = Vector()

        init {
            reloadPacks()
        }

        @JvmStatic
        private fun reloadIndex() {
            try {
                packNames.clear()
                parsedIndexes = ResourceFetcher.getFiles("lang-index.json")
                    .stream()
                    .map { IOStreamUtil.tryOpenStream(it) }
                    .filter { it != null }
                    .map { IOStreamUtil.newReader(it) }
                    .map {
                        try {
                            return@map JsonUtil.GSON_PARSER.fromJson<LangIndexModel>(it, LangIndexModel::class.java)
                        } catch (e: Exception) {
                            return@map null
                        }
                    }
                    .filter { it != null }
                    .collect(Collectors.toList()) as List<LangIndexModel>
            } catch (e: IOException) {
                ExceptionReporter.report(e, ExceptionReporter.ExceptionType.NATIVE)
            }
        }

        @JvmStatic
        private fun reloadTransition() {
            transitionMap.clear()
            localeRemap.keys.forEach(Consumer { locale: Locale? ->
                transitionMap[locale] = HashMap()
            })
            localeRemap.keys.forEach(Consumer { locale: Locale? ->
                transitionFiles[locale] = Vector()
            })
            parsedIndexes.stream()
                .map { obj: LangIndexModel -> obj.resources }
                .flatMap { it?.entries?.stream() ?: Stream.empty() }
                .map { (key, value): Map.Entry<String, String> ->
                    ImmutablePair(
                        key.replace("_", "-"),
                        value
                    )
                }
                .forEach { (key, value): ImmutablePair<String, String> ->
                    transitionFiles[Locale.forLanguageTag(key)]!!
                        .add(value)
                }
            transitionFiles.forEach { (locale: Locale?, strings: List<String>) ->
                transitionMap[locale] = strings.stream()
                    .map(Function { s: String? ->
                        try {
                            return@Function JsonUtil.GSON_PARSER.fromJson<Map<String?, Any?>>(
                                IOStreamUtil.newReader(
                                    Objects.requireNonNull<URL>(
                                        I18NManager::class.java.getClassLoader().getResource(s)
                                    ).openStream()
                                ), TypeToken.getParameterized(
                                    MutableMap::class.java,
                                    String::class.java,
                                    Any::class.java
                                ).type
                            )
                        } catch (e: Exception) {
                            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.IO)
                            return@Function HashMap<String?, Any?>()
                        }
                    })
                    .flatMap { m: Map<String?, Any?> -> m.entries.stream() }
                    .collect(
                        Collectors.toMap<Map.Entry<String?, Any?>, String, Any>(
                            { (key, _) -> key },
                            { (_, value) -> value }
                        ))
            }
            try {
                packNames.addAll(
                    parsedIndexes.stream()
                        .map { model: LangIndexModel? ->
                            Optional.ofNullable(model)
                                .map { obj: LangIndexModel -> obj.name }
                                .map { it?.key }
                                .orElse("<unnamed language pack>")
                        }
                        .map { translatable(it) }
                        .collect(Collectors.toList())
                )
            } catch (e: Exception) {
                ExceptionReporter.report(e, ExceptionReporter.ExceptionType.IO)
            }
        }

        @JvmStatic
        fun reloadPacks() {
            reloadIndex()
            reloadTransition()
        }

        @JvmStatic
        fun parseI18N(locale: Locale): String {
            return locale.language +
                    "_" +
                    locale.country
        }

        @JvmStatic
        fun addCusTranslate(loc: Locale?, key: String, value: String) {
            Optional.ofNullable(transitionMap[loc]).ifPresent { map: MutableMap<String, Any> ->
                map[key] = value
            }
        }

        @JvmStatic
        @Throws(NullPointerException::class)
        private fun getNotNull(locale: Locale, key: String, vararg args: Any): String {
            return try {
                val data = transitionMap[locale]!![key]
                if (data is String) Objects.requireNonNull(
                    java.lang.String.format(
                        data.toString(),
                        *args
                    )
                ) else throw ClassCastException()
            } catch (e: IllegalFormatException) {
                e.printStackTrace()
                "Format error: " + Objects.requireNonNull(transitionMap[locale]!![key])
            } catch (e: ClassCastException) {
                "Format error: " + Objects.requireNonNull(transitionMap[locale]!![key])
            }
        }

        /**
         * translatable string from transition files<br></br>
         * 从翻译文件获取字符串
         *
         * @param locale the target locale<br></br>目标低点
         * @param key    the string key<br></br>字符串key
         * @param args   format args<br></br>格式化参数
         * @return the fetched string<br></br>获取到的字符串
         */
        @JvmStatic
        operator fun get(locale: Locale, key: String, args: Array<out Any>): String {
            // TODO when string exists in the "locale" field
            try {
                return getNotNull(locale, key, *args)
            } catch (ignored: Exception) {
            }
            // TODO if not exists, find the string in the default locale (Locale.US)
            try {
                return getNotNull(Locale.US, key, *args)
            } catch (ignored: Exception) {
            }
            // TODO if the string don't exists at all, return the original key and format args
            return key + if (args.isNotEmpty()) args.toString() else ""
        }

        /**
         * get string without locale<br></br>
         * 不指定位置获取字符串
         * @param key  the string key<br></br>字符串key
         * @param args format args<br></br>格式化参数
         * @return the fetched string<br></br>获取到的字符串
         */
        @JvmStatic
        fun translatable(key: String?, vararg args: Any): Text {
            return TranslatableText(key, listOf(*args))
        }

        /**
         * create a [Text] shell for string<br></br>
         * 创建 [Text] 字符串外壳
         * @param text the internal string<br></br>内部字符串
         * @return the wrapped string<br></br>被包装的字符串
         */
        @JvmStatic
        fun fixed(text: String?): Text {
            return FixedText(text)
        }

        @JvmStatic
        val loadedPackNames: List<Text>
            get() = Collections.unmodifiableList(packNames)
    }
}
