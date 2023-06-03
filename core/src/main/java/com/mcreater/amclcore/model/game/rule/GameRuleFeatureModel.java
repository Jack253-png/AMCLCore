package com.mcreater.amclcore.model.game.rule;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameRuleFeatureModel {
    @SerializedName("is_demo_user")
    private Boolean isDemoUser;
    @SerializedName("has_custom_resolution")
    private Boolean hasCustomResolution;
}
