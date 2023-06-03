package com.mcreater.amclcore.util.date;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class StandardDate implements WrappedDate {
    public static final StandardDate DEFAULT = new StandardDate("2000-01-01T08:00:00.00000Z");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM dd hh mm ss");
    private static final Pattern PARSE_PATTERN = Pattern.compile("(?<year>[0-9].*)-(?<month>[0-9].*)-(?<day>[0-9].*)T(?<hour>[0-9].*):(?<min>[0-9].*):(?<sec>[0-9].*)\\.(?<dis>[0-9].*)Z");
    @Getter
    private String rawDate;

    public LocalDateTime convert() throws ParseException {
        Matcher matcher = PARSE_PATTERN.matcher(rawDate);
        if (!matcher.find()) return DEFAULT.convert();
        return DATE_FORMAT
                .parse(String.join(" ",
                        matcher.group("year"),
                        matcher.group("month"),
                        matcher.group("day"),
                        matcher.group("hour"),
                        matcher.group("min"),
                        matcher.group("sec")
                ))
                .toInstant()
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}
