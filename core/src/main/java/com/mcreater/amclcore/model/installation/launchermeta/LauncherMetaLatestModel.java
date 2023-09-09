package com.mcreater.amclcore.model.installation.launchermeta;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LauncherMetaLatestModel {
    private String release;
    private String snapshot;
}
