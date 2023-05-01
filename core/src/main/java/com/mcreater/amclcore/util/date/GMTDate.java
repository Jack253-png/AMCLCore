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
public class GMTDate implements WrappedDate {
    private static final GMTDate DEFAULT = new GMTDate("2000-01-01T08:00:00+00:00");
    private static final Pattern RAW_PARSE_PATTERN = Pattern.compile("(?<year>[0-9].*)-(?<month>[0-9].*)-(?<day>[0-9].*)T(?<hour>[0-9].*):(?<min>[0-9].*):(?<sec>[0-9].*)\\+(?<addrhour>[0-9].*):(?<addrmin>[0-9].*)");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM dd hh mm ss");
    @Getter
    private final String raw;

    public LocalDateTime convert() throws ParseException {
        Matcher matcher = RAW_PARSE_PATTERN.matcher(raw);
        if (!matcher.find()) return DEFAULT.convert();
        return DATE_FORMAT
                .parse(
                        String.join(
                                " ",
                                matcher.group("year"),
                                matcher.group("month"),
                                matcher.group("day"),
                                matcher.group("hour"),
                                matcher.group("min"),
                                matcher.group("sec")
                        )
                )
                .toInstant()
                .atOffset(ZoneOffset.ofHoursMinutes(
                        Integer.parseInt(matcher.group("addrhour")),
                        Integer.parseInt(matcher.group("addrmin"))
                ))
                .toLocalDateTime();
    }
}
