package com.mcreater.amclcore.model.i18n;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class LangIndexModel {
    private LangIndexNameModel name;
    private Map<String, String> resources;
}
