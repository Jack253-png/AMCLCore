package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.annotations.OAuthLoginModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@OAuthLoginModel
public class MinecraftResponseModel {
    private String identityToken;
}
