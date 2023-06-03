package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.annotations.OAuthLoginModel;
import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@OAuthLoginModel
@RequestModel
public class XBLTokenRequestModel {
    private String IssueInstant;
    private String NotAfter;
    private String Token;
    private XBLTokenDisplayClaimsModel DisplayClaims;

    @Builder
    @Data
    @OAuthLoginModel
    @RequestModel
    public static class XBLTokenDisplayClaimsModel {
        private List<XBLTokenUserHashModel> xui;
    }

    @Builder
    @Data
    @OAuthLoginModel
    @RequestModel
    public static class XBLTokenUserHashModel {
        private String uhs;
    }
}
