package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.annotations.OAuthLoginModel;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@OAuthLoginModel
public class XBLUserModel {
    private String token;
    private String hash;
}
