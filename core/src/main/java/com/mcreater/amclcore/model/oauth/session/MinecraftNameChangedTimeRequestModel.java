package com.mcreater.amclcore.model.oauth.session;

import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@RequestModel
public class MinecraftNameChangedTimeRequestModel {
    private String changedAt;
    private String createdAt;
    private boolean nameChangeAllowed;
}
