package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.annotations.OAuthLoginModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@OAuthLoginModel
public class XBLTokenRequestModel {
    private String IssueInstant;
    private String NotAfter;
    private String Token;
    private XBLTokenDisplayClaimsModel DisplayClaims;

    @Builder
    @Data
    @OAuthLoginModel
    public static class XBLTokenDisplayClaimsModel {
        private List<XBLTokenUserHashModel> xui;
    }

    @Builder
    @Data
    @OAuthLoginModel
    public static class XBLTokenUserHashModel {
        private String uhs;
    }
}
