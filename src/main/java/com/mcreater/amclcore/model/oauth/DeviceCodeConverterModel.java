package com.mcreater.amclcore.model.oauth;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DeviceCodeConverterModel {
    private TokenResponseModel model;
    private boolean isDevice;
}
