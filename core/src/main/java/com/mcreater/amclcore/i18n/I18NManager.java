package com.mcreater.amclcore.i18n;

import com.google.gson.reflect.TypeToken;
import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import com.mcreater.amclcore.model.i18n.LangIndexModel;
import com.mcreater.amclcore.util.IOStreamUtil;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

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

public class I18NManager {
    private static List<LangIndexModel> parsedIndexes = new Vector<>();
    private static final Map<Locale, String> localeRemap = Arrays.asList(SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE, FRANCE, GERMANY, ITALY, JAPAN, KOREA, UK, US, CANADA, CANADA_FRENCH)
            .stream()
            .collect(Collectors.toMap(
                    locale -> locale,
                    I18NManager::parseI18N
            ));
    @Getter
    private static final Map<Locale, Map<String, String>> transitionMap = new HashMap<>();
    private static final Map<Locale, List<String>> transitionFiles = new HashMap<>();

    static {
        reloadIndex();
        reloadTransition();
    }

    private static void reloadIndex() {
        try {
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
                .forEach(m -> m.forEach((s, s2) -> transitionFiles.get(Locale.forLanguageTag(s)).add(s2)));
        transitionFiles.forEach((locale, strings) -> transitionMap.put(locale, strings.stream()
                .map((Function<String, Map<String, String>>) s -> {
                    try {
                        return GSON_PARSER.fromJson(
                                new InputStreamReader(
                                        Objects.requireNonNull(
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
    }

    private static String parseI18N(Locale locale) {
        return new StringBuilder()
                .append(locale.getLanguage())
                .append("-")
                .append(locale.getCountry())
                .toString();
    }

    public static void test() {
        System.out.println(parseI18N(Locale.CHINA));
    }
}
