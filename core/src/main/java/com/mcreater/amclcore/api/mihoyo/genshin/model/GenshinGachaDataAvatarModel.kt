package com.mcreater.amclcore.api.mihoyo.genshin.model

import com.google.gson.annotations.SerializedName

data class GenshinGachaDataAvatarModel(
    var id: Int = 0,
    var name: String? = null,
    var icon: String? = null,
    var element: Int = 0,
    var level: Int = 0,
    @SerializedName("weapon_cat_id") var weaponCatId: Int = 0,
    @SerializedName("set_list") var setList: List<Any>? = null,
    @SerializedName("wiki_url") var wikiUrl: String? = null,
    @SerializedName("first_attr") var firstAttr: List<Any>? = null,
    @SerializedName("secondary_attr") var secondaryAttr: List<Any>? = null,
    @SerializedName("secondary_attr_name") var secondaryAttrName: List<Any>? = null,
    @SerializedName("head_icon") var headIcon: String? = null,
    @SerializedName("pc_icon") var pcIcon: String? = null
)