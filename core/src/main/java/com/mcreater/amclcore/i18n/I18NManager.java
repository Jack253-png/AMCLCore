package com.mcreater.amclcore.i18n;

import com.google.gson.reflect.TypeToken;
import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import com.mcreater.amclcore.model.i18n.LangIndexModel;
import com.mcreater.amclcore.model.i18n.LangIndexNameModel;
import com.mcreater.amclcore.util.IOStreamUtil;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;
import static java.util.Locale.CANADA;
import static java.util.Locale.CANADA_FRENCH;
import static java.util.Locale.FRANCE;
import static java.util.Locale.GERMANY;
import static java.util.Locale.ITALY;
import static java.util.Locale.JAPAN;
import static java.util.Locale.KOREA;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static java.util.Locale.TRADITIONAL_CHINESE;
import static java.util.Locale.UK;
import static java.util.Locale.US;
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
    @Getter
    private static final List<TranslatableText> packNames = new Vector<>();

    static {
        reloadIndex();
        reloadTransition();
    }

    private static void reloadIndex() {
        try {
            packNames.clear();
            parsedIndexes = Collections.list(I18NManager.class.getClassLoader().getResources("lang-index.json"))
                    .stream()
                    .map(IOStreamUtil::tryOpenStream)
                    .filter(Objects::nonNull)
                    .map(InputStreamReader::new)
                    .map(reader -> GSON_PARSER.fromJson(reader, LangIndexModel.class))
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
                                new InputStreamReader(
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
                            .map(I18NManager::get)
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.IO);
        }
    }

    private static String parseI18N(Locale locale) {
        return locale.getLanguage() +
                "_" +
                locale.getCountry();
    }

    private static String getNotNull(Locale locale, String key, Object... args) throws NullPointerException {
        try {
            return requireNonNull(String.format(transitionMap.get(locale).get(key), args));
        } catch (MissingFormatArgumentException e) {
            return requireNonNull(transitionMap.get(locale).get(key));
        }
    }

    /**
     * get string from transition files
     *
     * @param locale the target locale
     * @param key    the string key
     * @param args   format args
     * @return the fetched string
     */
    private static String get(Locale locale, String key, Object... args) {
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
     * get string without locale
     *
     * @param key  the string key
     * @param args format args
     * @return the fetched string
     */
    public static TranslatableText get(String key, Object... args) {
        return TranslatableText.builder()
                .key(key)
                .args(Arrays.asList(args))
                .build();
    }

    @Data
    @Builder
    public static class TranslatableText {
        private String key;
        private List<Object> args;

        /**
         * get string with locale
         *
         * @param locale the target locale
         * @return the fetched string
         */
        public String getText(Locale locale) {
            return get(locale, getKey(), args.toArray());
        }

        /**
         * get text with default locale {@link Locale#getDefault()}
         *
         * @return the fetched string
         */
        public String getText() {
            return getText(Locale.getDefault());
        }
    }
}
