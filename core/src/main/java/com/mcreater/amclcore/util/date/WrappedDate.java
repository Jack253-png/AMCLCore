package com.mcreater.amclcore.util.date;

import java.text.ParseException;
import java.time.LocalDateTime;

public interface WrappedDate {
    LocalDateTime convert() throws ParseException;
}
