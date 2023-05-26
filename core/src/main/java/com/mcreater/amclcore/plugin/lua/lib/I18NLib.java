package com.mcreater.amclcore.plugin.lua.lib;

import com.mcreater.amclcore.i18n.I18NManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static java.util.Locale.*;

public class I18NLib extends TwoArgFunction {
    private static final Map<String, Locale> localeMap = new HashMap<String, Locale>() {{
        Stream.of(TRADITIONAL_CHINESE, FRANCE, GERMANY, ITALY, JAPAN, KOREA, UK, US, CANADA, CANADA_FRENCH)
                .map(locale -> new ImmutablePair<>(I18NManager.parseI18N(locale), locale))
                .forEach(e -> put(e.getKey(), e.getValue()));
    }};

    public LuaValue call(LuaValue modName, LuaValue env) {
        LuaValue library = tableOf();
        library.set("_amclcore_i18n_addcustom", new ThreeArgFunction() {
            public LuaValue call(LuaValue loc, LuaValue key, LuaValue value) {
                if (loc.isnil()) argerror(1, translatable("core.api.lua.i18n.add_custom_translate.arg.1").getText());
                if (key.isnil()) argerror(2, translatable("core.api.lua.i18n.add_custom_translate.arg.2").getText());
                if (value.isnil()) argerror(3, translatable("core.api.lua.i18n.add_custom_translate.arg.3").getText());

                String jloc = loc.checkjstring();
                String jkey = key.checkjstring();
                String jvalue = value.checkjstring();

                AtomicBoolean isDec = new AtomicBoolean(false);
                localeMap.forEach((s, locale) -> {
                    if (s.equalsIgnoreCase(jloc)) {
                        I18NManager.addCusTranslate(locale, jkey, jvalue);
                        isDec.set(true);
                    }
                });
                if (!isDec.get()) I18NManager.addCusTranslate(US, jkey, jvalue);

                return LuaValue.NIL;
            }
        });
        env.set("amclcore_api/i18n", library);
        env.get("package").get("loaded").set("amclcore_api/i18n", library);
        return library;
    }
}
