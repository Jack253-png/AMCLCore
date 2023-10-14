package com.mcreater.amclcore.api.mihoyo.genshin.model

import com.google.gson.annotations.SerializedName

data class GenshinGachaDataModel(
    @SerializedName("all_avatar") var avatar: List<GenshinGachaDataAvatarModel?>? = null,
    @SerializedName("all_weapon") var weapon: List<GenshinGachaDataWeaponModel>? = null,
    @SerializedName("all_set") val set: List<GenshinGachaDataSetModel>? = null,
    @SerializedName("weapon_level") val weaponLevel: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("set_level") val setLevel: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("avatar_element") val avatarElement: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("weapon_cat") val weaponCat: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("avatar_tag") var avatarTag: MutableList<GenshinGachaDataLevelModel?>? = null
)
