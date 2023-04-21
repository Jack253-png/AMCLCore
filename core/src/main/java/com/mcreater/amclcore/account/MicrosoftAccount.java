package com.mcreater.amclcore.account;

import com.mcreater.amclcore.concurrent.AbstractAction;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel;
import com.mcreater.amclcore.model.oauth.MinecraftRequestModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;
import com.mcreater.amclcore.util.HttpClientWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import static com.mcreater.amclcore.account.auth.OAuth.minecraftProfileUrl;
import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.FunctionUtil.genSelfFunction;

public class MicrosoftAccount extends AbstractAccount {
    private String refreshToken;
    private MinecraftRequestModel internalToken;
    private MinecraftProfileRequestModel profile;

    private MicrosoftAccount(@NotNull MinecraftRequestModel minecraftUser, String refreshToken) {
        super(minecraftUser.getAccessToken());
        this.refreshToken = refreshToken;
        this.internalToken = minecraftUser;
    }

    public static MicrosoftAccount create(@NotNull MinecraftRequestModel minecraftUser, @NotNull DeviceCodeConverterModel deviceCode) {
        return new MicrosoftAccount(minecraftUser, deviceCode.getModel().getRefreshToken());
    }

    public AbstractAction refreshAsync() {
        return null;
    }

    public FetchProfileTask fetchProfileAsync() {
        return new FetchProfileTask();
    }

    public String getAccountName() {
        return Optional.ofNullable(profile)
                .map(MinecraftProfileRequestModel::getName)
                .map(genSelfFunction(this::setAccountName))
                .orElse(null);
    }

    public UUID getUuid() {
        return Optional.ofNullable(profile)
                .map(MinecraftProfileRequestModel::getId)
                .map(genSelfFunction(this::setUuid))
                .orElse(null);
    }

    public class FetchProfileTask extends AbstractAction {
        private FetchProfileTask() {
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.fetchProfile.name");
        }

        protected void execute() throws Exception {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                    .uri(minecraftProfileUrl)
                    .header("Authorization", String.format("%s %s", internalToken.getTokenType(), getAccessToken()))
                    .setRetry(5)
                    .sendAndReadJson(MinecraftProfileRequestModel.class);
        }
    }
}
