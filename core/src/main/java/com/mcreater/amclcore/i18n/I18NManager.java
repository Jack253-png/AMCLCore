package com.mcreater.amclcore.i18n;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import com.mcreater.amclcore.model.i18n.LangIndexModel;
import com.mcreater.amclcore.util.IOStreamUtil;

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
import java.util.stream.Collectors;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;
import static java.util.Locale.CANADA;
import static java.util.Locale.CANADA_FRENCH;
import static java.util.Locale.CHINA;
import static java.util.Locale.FRANCE;
import static java.util.Locale.GERMANY;
import static java.util.Locale.ITALY;
import static java.util.Locale.JAPAN;
import static java.util.Locale.KOREA;
import static java.util.Locale.PRC;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static java.util.Locale.TAIWAN;
import static java.util.Locale.TRADITIONAL_CHINESE;
import static java.util.Locale.UK;
import static java.util.Locale.US;

public class I18NManager {
    private static List<LangIndexModel> parsedIndexes = new Vector<>();
    private static final Map<Locale, String> localeRemap = Arrays.asList(SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE, FRANCE, GERMANY, ITALY, JAPAN, KOREA, CHINA, PRC, TAIWAN, UK, US, CANADA, CANADA_FRENCH)
            .parallelStream()
            .collect(Collectors.toMap(
                    locale -> locale,
                    I18NManager::parseI18N
            ));
    private static final Map<Locale, Map<String, String>> transitionMap = new HashMap<>();

    static {
        reloadIndex();
    }

    private static void reloadIndex() {
        try {
            parsedIndexes = Collections.list(I18NManager.class.getClassLoader().getResources("lang-index.json"))
                    .parallelStream()
                    .map(IOStreamUtil::tryOpenStream)
                    .filter(Objects::nonNull)
                    .map(InputStreamReader::new)
                    .map(reader -> GSON_PARSER.fromJson(reader, LangIndexModel.class))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.NATIVE);
        }
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
