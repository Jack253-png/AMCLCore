package com.mcreater.amclcore.api.mihoyo.genshin

import com.mcreater.amclcore.api.mihoyo.genshin.cn.GenshinGachaApi
import com.mcreater.amclcore.api.mihoyo.genshin.model.GenshinGachaModel
import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.i18n.I18NManager
import com.mcreater.amclcore.i18n.Text
import com.mcreater.amclcore.util.HttpClientWrapper
import java.io.IOException
import java.net.URISyntaxException
import java.util.*

interface AbstractGenshinGachaApi {
    fun getGenshinGachaUrl(): String?

    @Throws(URISyntaxException::class, IOException::class)
    fun genshinGachaFetchAsync(): GenshinGachaFetchTask {
        return genshinGachaFetchAsync(Locale.getDefault())
    }

    @Throws(URISyntaxException::class, IOException::class)
    fun genshinGachaFetchAsync(locale: Locale?): GenshinGachaFetchTask {
        return GenshinGachaFetchTask(
            getGenshinGachaUrl(),
            locale
        )
    }


    class GenshinGachaFetchTask(private val url: String? = null, private val locale: Locale? = null) :
        AbstractTask<GenshinGachaModel?>() {
        @Throws(Exception::class)
        override fun call(): GenshinGachaModel {
            return HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .timeout(5000)
                .reqTimeout(5000)
                .uri(url)
                .uriParam("lang", locale.toString().lowercase(Locale.getDefault()).replace("_", "-"))
                .sendAndReadJson(GenshinGachaModel::class.java)
        }

        override fun getTaskName(): Text {
            return I18NManager.fixed("")
        }
    }


    companion object {
        @JvmStatic
        fun getCnInstance(): AbstractGenshinGachaApi {
            return GenshinGachaApi()
        }
    }
}