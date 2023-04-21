package com.mcreater.amclcore.account;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.AbstractAction;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.exceptions.oauth.OAuthXBLNotFoundException;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel;
import com.mcreater.amclcore.model.oauth.MinecraftRequestModel;
import com.mcreater.amclcore.model.oauth.TokenResponseModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;
import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.FunctionUtil.genSelfFunction;
import static com.mcreater.amclcore.util.JsonUtil.createPair;
import static java.util.Objects.requireNonNull;

public class MicrosoftAccount extends AbstractAccount {
    public interface Accessor {
        Accessor INSTANCE = new Accessor() {
            public String getMinecraftProfileUrl() {
                throw new UnsupportedOperationException("not implemented yet");
            }

            public OAuth.OAuthLoginInternalTask createLoginInternalTask(DeviceCodeConverterModel model) {
                throw new UnsupportedOperationException("not implemented yet");
            }

            public String createClientID() {
                throw new UnsupportedOperationException("not implemented yet");
            }

            public String getTokenUrl() {
                throw new UnsupportedOperationException("not implemented yet");
            }
        };
        String getMinecraftProfileUrl();
        OAuth.OAuthLoginInternalTask createLoginInternalTask(DeviceCodeConverterModel model);
        String createClientID();
        String getTokenUrl();
    }
    @Setter
    private static Accessor apiAccessor = Accessor.INSTANCE;
    private String refreshToken;
    private final String tokenType;
    private MinecraftProfileRequestModel profile;

    private MicrosoftAccount(@NotNull MinecraftRequestModel minecraftUser, String refreshToken) {
        super(minecraftUser.getAccessToken());
        this.refreshToken = refreshToken;
        this.tokenType = minecraftUser.getTokenType();
    }

    public static MicrosoftAccount create(@NotNull MinecraftRequestModel minecraftUser, @NotNull DeviceCodeConverterModel deviceCode) {
        return new MicrosoftAccount(minecraftUser, deviceCode.getModel().getRefreshToken());
    }

    /**
     * create account refresh task<br>
     * 创建账户刷新任务
     * @return the created task<br>被创建的任务
     */
    public AbstractAction refreshAsync() {
        return new RefreshAccountTask();
    }

    /**
     * create fetch profile task<br>
     * 创建档案获取任务
     *
     * @return the created task<br>被创建的任务
     */
    public FetchProfileTask fetchProfileAsync() {
        return new FetchProfileTask();
    }

    public List<MinecraftProfileRequestModel.MinecraftProfileSkinModel> getSkins() {
        return Optional.ofNullable(profile)
                .map(MinecraftProfileRequestModel::getSkins)
                .orElse(new Vector<>());
    }

    public List<MinecraftProfileRequestModel.MinecraftProfileCapeModel> getCapes() {
        return Optional.ofNullable(profile)
                .map(MinecraftProfileRequestModel::getCapes)
                .orElse(new Vector<>());
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
            profile = requireNonNull(HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                    .uri(apiAccessor.getMinecraftProfileUrl())
                    .header("Authorization", String.format("%s %s", tokenType, getAccessToken()))
                    .setRetry(5)
                    .sendAndReadJson(MinecraftProfileRequestModel.class));
        }
    }

    public class RefreshAccountTask extends AbstractAction {
        private RefreshAccountTask() {
            canBind = false;
        }
        protected void execute() throws Exception {
            TokenResponseModel model;
            // TODO Refresh with RefreshToken
            {
                setState(TaskState.<Void>builder()
                        .totalStage(7)
                        .currentStage(1)
                        .message(translatable("core.oauth.refreshAccount.pre.text"))
                        .build()
                );
                model = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                        .uri(apiAccessor.getTokenUrl())
                        .entityEncodedUrl(
                                createPair("client_id", apiAccessor.createClientID()),
                                createPair("refresh_token", refreshToken),
                                createPair("grant_type", "refresh_token")
                        )
                        .timeout(5000)
                        .reqTimeout(5000)
                        .sendAndReadJson(TokenResponseModel.class);
            }
            // TODO Update refresh token
            {
                setState(TaskState.<Void>builder()
                        .totalStage(7)
                        .currentStage(2)
                        .message(translatable("core.oauth.refreshAccount.refreshingToken.text"))
                        .build()
                );
                refreshToken = model.getRefreshToken();
            }
            // TODO Fork internal task for fetching AccessToken
            {
                MicrosoftAccount accountNew = apiAccessor.createLoginInternalTask(
                                DeviceCodeConverterModel.builder()
                                        .model(model)
                                        .isDevice(true)
                                        .build())
                        .bindTo(this)
                        .fork()
                        .get()
                        .orElseThrow(OAuthXBLNotFoundException::new);
                setAccessToken(accountNew.getAccessToken());
            }
            // TODO Update profile
            {
                setState(TaskState.<Void>builder()
                        .totalStage(7)
                        .currentStage(6)
                        .message(translatable("core.oauth.refreshAccount.updateProfile.text"))
                        .build()
                );
                fetchProfileAsync()
                        .bindTo(this)
                        .fork()
                        .get();
            }
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.refreshAccount.name");
        }
    }
}
