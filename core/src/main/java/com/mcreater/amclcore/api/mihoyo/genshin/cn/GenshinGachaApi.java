package com.mcreater.amclcore.api.mihoyo.genshin.cn;

import com.mcreater.amclcore.api.mihoyo.genshin.AbstractGenshinGachaApi;

public class GenshinGachaApi implements AbstractGenshinGachaApi {
    public String getGenshinGachaUrl() {
        return "sg-public-api.hoyolab.com/event/simulatoros/config";
    }
}
