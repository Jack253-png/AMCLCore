package com.mcreater.amclcore.i18n

import java.util.*


data class TranslatableText(
    val key: String? = null,
    val args: List<Any>? = null
) : Text {
    override fun getText(locale: Locale?): String {
        return I18NManager[locale!!, key!!, args!!.toTypedArray()]
    }
}
