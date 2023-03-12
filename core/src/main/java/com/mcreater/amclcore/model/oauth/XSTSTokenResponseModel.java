package com.mcreater.amclcore.model.oauth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class XSTSTokenResponseModel {
    private XSTSTokenResponsePropertiesModel Properties;
    private String RelyingParty;
    private String TokenType;

    @Builder
    @Data
    public static class XSTSTokenResponsePropertiesModel {
        private String SandboxId;
        private List<String> UserTokens;
    }
}
