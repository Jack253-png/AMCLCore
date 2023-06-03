package com.mcreater.amclcore.model.oauth.session;

import com.mcreater.amclcore.annotations.RequestModel;
import com.mcreater.amclcore.util.date.StandardDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@RequestModel
public class MinecraftNameChangedTimeRequestModel {
    private StandardDate changedAt;
    private StandardDate createdAt;
    private boolean nameChangeAllowed;
}
