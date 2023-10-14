package com.mcreater.amclcore.api.mihoyo.genshin.cn

import com.mcreater.amclcore.api.mihoyo.genshin.AbstractGenshinGachaApi

class GenshinGachaApi : AbstractGenshinGachaApi {
    override fun getGenshinGachaUrl(): String {
        return "sg-public-api.hoyolab.com/event/simulatoros/config"
    }
}