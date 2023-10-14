package com.mcreater.amclcore.plugin.lua.lib

import com.mcreater.amclcore.i18n.I18NManager
import org.apache.commons.lang3.tuple.ImmutablePair
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Stream


class I18NLib : TwoArgFunction() {
    override fun call(modName: LuaValue, env: LuaValue): LuaValue {
        val library: LuaValue = tableOf()
        library["_amclcore_i18n_addcustom"] = object : ThreeArgFunction() {
            override fun call(loc: LuaValue, key: LuaValue, value: LuaValue): LuaValue {
                if (loc.isnil()) argerror(
                    1,
                    I18NManager.translatable("core.api.lua.i18n.add_custom_translate.arg.1").text
                )
                if (key.isnil()) argerror(
                    2,
                    I18NManager.translatable("core.api.lua.i18n.add_custom_translate.arg.2").text
                )
                if (value.isnil()) argerror(
                    3,
                    I18NManager.translatable("core.api.lua.i18n.add_custom_translate.arg.3").text
                )
                val jloc = loc.checkjstring()
                val jkey = key.checkjstring()
                val jvalue = value.checkjstring()
                val isDec = AtomicBoolean(false)
                localeMap.forEach { (s: String, locale: Locale?) ->
                    if (s.equals(jloc, ignoreCase = true)) {
                        I18NManager.addCusTranslate(locale, jkey, jvalue)
                        isDec.set(true)
                    }
                }
                if (!isDec.get()) I18NManager.addCusTranslate(Locale.US, jkey, jvalue)
                return NIL
            }
        }
        env["amclcore_api/i18n"] = library
        env["package"]["loaded"]["amclcore_api/i18n"] = library
        return library
    }

    companion object {
        private val localeMap: Map<String, Locale> = object : HashMap<String, Locale>() {
            init {
                Stream.of(
                    Locale.TRADITIONAL_CHINESE,
                    Locale.FRANCE,
                    Locale.GERMANY,
                    Locale.ITALY,
                    Locale.JAPAN,
                    Locale.KOREA,
                    Locale.UK,
                    Locale.US,
                    Locale.CANADA,
                    Locale.CANADA_FRENCH
                )
                    .map { ImmutablePair(I18NManager.parseI18N(it), it) }
                    .forEach { (key, value): ImmutablePair<String, Locale> ->
                        put(key, value)
                    }
            }
        }
    }
}
