package com.mcreater.amclcore.model.installation.launchermeta;

import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@RequestModel
@Builder
public class LauncherMetaModel {
    private LauncherMetaLatestModel latest;
    private List<LauncherMetaVersionModel> versions;
}
