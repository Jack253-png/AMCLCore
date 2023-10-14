package com.mcreater.amclcore.i18n

import java.util.*

data class FixedText(
    var internalText: String? = null
) : Text {
    override fun getText(locale: Locale?): String {
        return internalText!!
    }
}
