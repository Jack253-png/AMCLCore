package com.mcreater.amclcore.api.mihoyo.genshin.model

import com.google.gson.annotations.SerializedName

data class GenshinGachaDataModel(
    @SerializedName("all_avatar") var avatar: List<GenshinGachaDataAvatarModel?>? = null,
    @SerializedName("all_weapon") var weapon: List<GenshinGachaDataWeaponModel>? = null,
    @SerializedName("all_set") var set: List<GenshinGachaDataSetModel>? = null,
    @SerializedName("weapon_level") var weaponLevel: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("set_level") var setLevel: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("avatar_element") var avatarElement: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("weapon_cat") var weaponCat: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("avatar_tag") var avatarTag: MutableList<GenshinGachaDataLevelModel?>? = null
)
