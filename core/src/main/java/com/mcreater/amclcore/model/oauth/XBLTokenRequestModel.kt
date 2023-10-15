package com.mcreater.amclcore.model.oauth

import com.mcreater.amclcore.annotations.OAuthLoginModel
import com.mcreater.amclcore.annotations.RequestModel

@OAuthLoginModel
@RequestModel
data class XBLTokenRequestModel(
    var IssueInstant: String? = null,
    var NotAfter: String? = null,
    var Token: String? = null,
    var DisplayClaims: XBLTokenDisplayClaimsModel? = null
) {
    @OAuthLoginModel
    @RequestModel
    data class XBLTokenDisplayClaimsModel(
        var xui: List<XBLTokenUserHashModel>? = null
    )

    @OAuthLoginModel
    @RequestModel
    data class XBLTokenUserHashModel(
        var uhs: String? = null
    )
}
