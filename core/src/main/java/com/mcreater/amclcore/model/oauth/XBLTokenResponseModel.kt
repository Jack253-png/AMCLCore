package com.mcreater.amclcore.model.oauth

import com.mcreater.amclcore.annotations.OAuthLoginModel

@OAuthLoginModel
data class XBLTokenResponseModel(
    private var Properties: XBLTokenResponsePropertiesModel? = null,
    var RelyingParty: String? = null,
    var TokenType: String? = null
) {
    @OAuthLoginModel
    data class XBLTokenResponsePropertiesModel(
        var AuthMethod: String? = null,
        var SiteName: String? = null,
        var RpsTicket: String? = null
    )
}