package com.mcreater.amclcore.model.i18n

data class LangIndexModel(
    var name: LangIndexNameModel? = null,
    var resources: Map<String, String>? = null
)
