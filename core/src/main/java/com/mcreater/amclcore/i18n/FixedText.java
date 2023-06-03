package com.mcreater.amclcore.i18n;

import lombok.Builder;
import lombok.Data;

import java.util.Locale;

@Data
@Builder
public class FixedText implements Text {
    private String internalText;

    public String getText(Locale locale) {
        return internalText;
    }
}
