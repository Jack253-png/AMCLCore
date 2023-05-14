package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.annotations.ConfigModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@ConfigModel
public class ConfigMainModel {
    private ConfigLaunchModel launchConfig;
    private List<AbstractAccount> accounts;
    private int selectedAccountIndex;

    public Optional<AbstractAccount> getSelectedAccount() {
        if (selectedAccountIndex < 0) return Optional.empty();
        return Optional.ofNullable(accounts.get(selectedAccountIndex));
    }
}
