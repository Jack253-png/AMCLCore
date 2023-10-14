package com.mcreater.amclcore.i18n

import java.util.*


interface Text {
    fun getText(locale: Locale?): String?
    val text: String?
        get() = getText(Locale.getDefault())
}