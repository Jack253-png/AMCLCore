package com.mcreater.amclcore.model.oauth;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class XBLUserModel {
    private String token;
    private String hash;
}
