package com.mcreater.amclcore.i18n;

import com.google.gson.reflect.TypeToken;
import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import com.mcreater.amclcore.model.i18n.LangIndexModel;
import com.mcreater.amclcore.model.i18n.LangIndexNameModel;
import com.mcreater.amclcore.util.IOStreamUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;
import static java.util.Locale.*;
import static java.util.Objects.requireNonNull;

public class I18NManager {
    private static List<LangIndexModel> parsedIndexes = new Vector<>();
    private static final Map<Locale, String> localeRemap = Stream.of(SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE, FRANCE, GERMANY, ITALY, JAPAN, KOREA, UK, US, CANADA, CANADA_FRENCH)
            .collect(Collectors.toMap(
                    locale -> locale,
                    I18NManager::parseI18N
            ));
    private static final Map<Locale, Map<String, String>> transitionMap = new HashMap<>();
    private static final Map<Locale, List<String>> transitionFiles = new HashMap<>();
    private static final List<Text> packNames = new Vector<>();

    static {
        reloadPacks();
    }

    private static void reloadIndex() {
        try {
            packNames.clear();
            parsedIndexes = Collections.list(I18NManager.class.getClassLoader().getResources("lang-index.json"))
                    .stream()
                    .map(IOStreamUtil::tryOpenStream)
                    .filter(Objects::nonNull)
                    .map(IOStreamUtil::newReader)
                    .map(reader -> {
                        try {
                            return GSON_PARSER.fromJson(reader, LangIndexModel.class);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.NATIVE);
        }
    }

    private static void reloadTransition() {
        transitionMap.clear();
        localeRemap.keySet().forEach(locale -> transitionMap.put(locale, new HashMap<>()));
        localeRemap.keySet().forEach(locale -> transitionFiles.put(locale, new Vector<>()));
        parsedIndexes.stream()
                .map(LangIndexModel::getResources)
                .flatMap(m -> m.entrySet().stream())
                .map(e -> new ImmutablePair<>(e.getKey().replace("_", "-"), e.getValue()))
                .forEach(e -> transitionFiles.get(Locale.forLanguageTag(e.getKey())).add(e.getValue()));
        transitionFiles.forEach((locale, strings) -> transitionMap.put(locale, strings.stream()
                .map((Function<String, Map<String, String>>) s -> {
                    try {
                        return GSON_PARSER.fromJson(
                                IOStreamUtil.newReader(
                                        requireNonNull(
                                                I18NManager.class.getClassLoader().getResource(s)
                                        ).openStream()
                                ), TypeToken.getParameterized(
                                        Map.class,
                                        String.class,
                                        String.class).getType()
                        );
                    } catch (Exception e) {
                        ExceptionReporter.report(e, ExceptionReporter.ExceptionType.IO);
                        return new HashMap<>();
                    }
                })
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        ));
        try {
            packNames.addAll(
                    parsedIndexes.stream()
                            .map(model -> Optional.ofNullable(model)
                                    .map(LangIndexModel::getName)
                                    .map(LangIndexNameModel::getKey)
                                    .orElse("<unnamed language pack>"))
                            .map(I18NManager::translatable)
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.IO);
        }
    }

    public static void reloadPacks() {
        reloadIndex();
        reloadTransition();
    }

    private static String parseI18N(Locale locale) {
        return locale.getLanguage() +
                "_" +
                locale.getCountry();
    }

    private static String getNotNull(Locale locale, String key, Object... args) throws NullPointerException {
        try {
            return requireNonNull(String.format(transitionMap.get(locale).get(key), args));
        } catch (MissingFormatArgumentException | IllegalFormatConversionException e) {
            return "Format error: " + requireNonNull(transitionMap.get(locale).get(key));
        }
    }

    /**
     * translatable string from transition files<br>
     * 从翻译文件获取字符串
     *
     * @param locale the target locale<br>目标低点
     * @param key    the string key<br>字符串key
     * @param args   format args<br>格式化参数
     * @return the fetched string<br>获取到的字符串
     */
    static String get(Locale locale, String key, Object... args) {
        // TODO when string exists in the "locale" field
        try {
            return getNotNull(locale, key, args);
        } catch (Exception ignored) {
        }
        // TODO if not exists, find the string in the default locale (Locale.US)
        try {
            return getNotNull(US, key, args);
        } catch (Exception ignored) {
        }
        // TODO if the string don't exists at all, return the original key and format args
        return key + (args.length > 0 ? Arrays.toString(args) : "");
    }

    /**
     * get string without locale<br>
     * 不指定位置获取字符串
     * @param key  the string key<br>字符串key
     * @param args format args<br>格式化参数
     * @return the fetched string<br>获取到的字符串
     */
    public static Text translatable(String key, Object... args) {
        return TranslatableText.builder()
                .key(key)
                .args(Arrays.asList(args))
                .build();
    }

    /**
     * create a {@link Text} shell for string<br>
     * 创建 {@link Text} 字符串外壳
     * @param text the internal string<br>内部字符串
     * @return the wrapped string<br>被包装的字符串
     */
    public static Text fixed(String text) {
        return FixedText.builder()
                .internalText(text)
                .build();
    }

    public static List<Text> getLoadedPackNames() {
        return Collections.unmodifiableList(packNames);
    }
}
