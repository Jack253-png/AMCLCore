package com.mcreater.amclcore.model.oauth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenCheckModel {
    private String grant_type;
    private String client_id;
    private String code;
}
