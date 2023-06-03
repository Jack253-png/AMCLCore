package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.annotations.OAuthLoginModel;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@OAuthLoginModel
public class XBLTokenResponseModel {
    private XBLTokenResponsePropertiesModel Properties;
    private String RelyingParty;
    private String TokenType;

    @Builder
    @Data
    @OAuthLoginModel
    public static class XBLTokenResponsePropertiesModel {
        private String AuthMethod;
        private String SiteName;
        private String RpsTicket;
    }
}
