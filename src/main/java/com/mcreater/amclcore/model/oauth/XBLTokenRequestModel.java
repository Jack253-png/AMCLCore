package com.mcreater.amclcore.model.oauth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class XBLTokenRequestModel {
    private String IssueInstant;
    private String NotAfter;
    private String Token;
    private XBLTokenDisplayClaimsModel DisplayClaims;

    @Builder
    @Data
    public static class XBLTokenDisplayClaimsModel {
        private List<XBLTokenUserHashModel> xui;
    }

    @Builder
    @Data
    public static class XBLTokenUserHashModel {
        private String uhs;
    }
}
