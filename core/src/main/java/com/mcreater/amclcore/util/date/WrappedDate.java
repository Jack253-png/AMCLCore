package com.mcreater.amclcore.util.date;

import java.text.ParseException;
import java.util.Date;

public interface WrappedDate {
    Date convert() throws ParseException;
}
