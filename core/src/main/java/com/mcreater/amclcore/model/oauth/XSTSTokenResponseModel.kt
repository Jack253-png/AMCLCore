package com.mcreater.amclcore.model.oauth

import com.mcreater.amclcore.annotations.OAuthLoginModel

@OAuthLoginModel
data class XSTSTokenResponseModel(
    var Properties: XSTSTokenResponsePropertiesModel? = null,
    var RelyingParty: String? = null,
    var TokenType: String? = null
) {
    @OAuthLoginModel
    data class XSTSTokenResponsePropertiesModel(
        var SandboxId: String? = null,
        var UserTokens: List<String>? = null
    )
}
