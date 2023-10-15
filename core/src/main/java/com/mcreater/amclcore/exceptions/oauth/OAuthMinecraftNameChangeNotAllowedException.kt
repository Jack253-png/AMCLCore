package com.mcreater.amclcore.exceptions.oauth

import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import java.io.IOException


data class OAuthMinecraftNameChangeNotAllowedException(
    val lastChange: String? = null,
    val nextChange: String? = null,
    val deltaTime: Long = 0
) : IOException() {
    override fun toString(): String {
        return translatable(
            "core.exceptions.oauth.name_change_not_allowed",
            lastChange!!, nextChange!!, deltaTime
        ).text!!
    }
}
