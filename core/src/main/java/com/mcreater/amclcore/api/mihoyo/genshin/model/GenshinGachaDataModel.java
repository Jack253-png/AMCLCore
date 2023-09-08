package com.mcreater.amclcore.api.mihoyo.genshin.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenshinGachaDataModel {
    @SerializedName("all_avatar")
    private List<GenshinGachaDataAvatarModel> avatar;
    @SerializedName("all_weapon")
    private List<GenshinGachaDataWeaponModel> weapon;
    @SerializedName("all_set")
    private List<GenshinGachaDataModel> set;
    @SerializedName("weapon_level")
    private List<GenshinGachaDataLevelModel> weaponLevel;
    @SerializedName("set_level")
    private List<GenshinGachaDataLevelModel> setLevel;
    @SerializedName("avatar_element")
    private List<GenshinGachaDataLevelModel> avatarElement;
    @SerializedName("weapon_cat")
    private List<GenshinGachaDataLevelModel> weaponCat;
    @SerializedName("avatar_tag")
    private List<GenshinGachaDataLevelModel> avatarTag;


}
