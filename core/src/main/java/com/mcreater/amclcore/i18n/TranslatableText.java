package com.mcreater.amclcore.i18n;


import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
@Builder
public class TranslatableText implements Text {
    private String key;
    private List<Object> args;

    /**
     * translatable string with locale
     *
     * @param locale the target locale
     * @return the fetched string
     */
    public String getText(Locale locale) {
        return I18NManager.get(locale, getKey(), args.toArray());
    }
}
