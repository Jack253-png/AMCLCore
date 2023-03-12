package com.mcreater.amclcore.model.oauth;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class XBLTokenResponseModel {
    private XBLTokenResponsePropertiesModel Properties;
    private String RelyingParty;
    private String TokenType;

    @Builder
    @Data
    public static class XBLTokenResponsePropertiesModel {
        private String AuthMethod;
        private String SiteName;
        private String RpsTicket;
    }
}
