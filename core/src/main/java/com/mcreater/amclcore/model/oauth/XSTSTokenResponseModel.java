package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.annotations.OAuthLoginModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@OAuthLoginModel
public class XSTSTokenResponseModel {
    private XSTSTokenResponsePropertiesModel Properties;
    private String RelyingParty;
    private String TokenType;

    @Builder
    @Data
    @OAuthLoginModel
    public static class XSTSTokenResponsePropertiesModel {
        private String SandboxId;
        private List<String> UserTokens;
    }
}
