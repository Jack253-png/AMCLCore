package com.mcreater.amclcore.account;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.exceptions.oauth.OAuthXBLNotFoundException;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel;
import com.mcreater.amclcore.model.oauth.MinecraftRequestModel;
import com.mcreater.amclcore.model.oauth.TokenResponseModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangeableRequestModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;
import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.FunctionUtil.genSelfFunction;
import static com.mcreater.amclcore.util.JsonUtil.createPair;

/**
 * API Documentation<br>API 文档<br>
 * <a href="https://wiki.vg/Mojang_API">Link<br>链接</a>
 */
public class MicrosoftAccount extends AbstractAccount {
    private static final Pattern NAME_PATTERN = Pattern.compile("([^a-zA-Z0-9_].*)");

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

            public String getMinecraftCapeModifyUrl() {
                throw new UnsupportedOperationException("not implemented yet");
            }

            public String getMinecraftNameCheckUrl() {
                throw new UnsupportedOperationException("not implemented yet");
            }

            public String getMinecraftNameChangeUrl() {
                throw new UnsupportedOperationException("not implemented yet");
            }

            public String getMinecraftNameChangeStateUrl() {
                throw new UnsupportedOperationException("not implemented yet");
            }
        };

        String getMinecraftProfileUrl();

        OAuth.OAuthLoginInternalTask createLoginInternalTask(DeviceCodeConverterModel model);

        String createClientID();

        String getTokenUrl();

        String getMinecraftCapeModifyUrl();

        String getMinecraftNameCheckUrl();

        String getMinecraftNameChangeUrl();

        String getMinecraftNameChangeStateUrl();
    }

    private static Accessor apiAccessor = Accessor.INSTANCE;

    public static void setApiAccessor(@NotNull Accessor apiAccessor) {
        if (MicrosoftAccount.apiAccessor == Accessor.INSTANCE) MicrosoftAccount.apiAccessor = apiAccessor;
    }

    @Getter
    private String refreshToken;
    @Getter
    private final String tokenType;
    private MinecraftProfileRequestModel profile;

    private MicrosoftAccount(@NotNull MinecraftRequestModel minecraftUser, @NotNull String refreshToken) {
        super(minecraftUser.getAccessToken());
        this.refreshToken = refreshToken;
        this.tokenType = minecraftUser.getTokenType();
    }

    private MicrosoftAccount(@NotNull String accessToken, @NotNull String refreshToken, @NotNull String tokenType) {
        super(accessToken);
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
    }

    public static MicrosoftAccount create(@NotNull MinecraftRequestModel minecraftUser, @NotNull DeviceCodeConverterModel deviceCode) {
        return new MicrosoftAccount(minecraftUser, deviceCode.getModel().getRefreshToken());
    }

    public static MicrosoftAccount create(@NotNull String accessToken, @NotNull String refreshToken, @NotNull String tokenType) {
        return new MicrosoftAccount(accessToken, refreshToken, tokenType);
    }

    /**
     * create account refresh task<br>
     * 创建账户刷新任务
     *
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

    /**
     * create check profile's AccessToken is outdated or not task<br>
     * 创建检查账户 AccessToken 是否失效与否的任务
     *
     * @return the created task<br>被创建的任务
     */
    public ValidateAccountTask validateAccountAsync() {
        return new ValidateAccountTask();
    }

    /**
     * create disable cape task<br>
     * 创建禁用披风任务
     *
     * @return the created task<br>被创建的任务
     */
    public DisableAccountCapeTask disableAccountCapeAsync() {
        return new DisableAccountCapeTask();
    }

    /**
     * create check account name changeable task<br>
     * 创建检查账户名是否可以改变的任务
     *
     * @return the created task<br>被创建的任务
     */
    public NameChangeableCheckTask checkAccountNameChangeableAsync(@NotNull String newName) {
        return new NameChangeableCheckTask(newName);
    }

    /**
     * create change account name task<br>
     * 创建更改账户名任务
     *
     * @param newName the created task<br>被创建的任务
     */
    public NameChangeTask changeAccountNameAsync(@NotNull String newName) {
        return new NameChangeTask(newName);
    }

    /**
     * create check account name changed time task<br>
     * 创建检查账户名改变时间的任务
     *
     * @return the created task<br>被创建的任务
     */
    public AccountNameChangedTimeCheckTask checkAccountNameChangedTimeAsync() {
        return new AccountNameChangedTimeCheckTask();
    }

    /**
     * check account name is allowed<br>
     * 检查账户名是否允许
     * rule: letter / number / underline (_) / length <= 16<br>
     * 规则: 大小写字母 / 数字 / 下划线(_) / 长度 <= 16
     *
     * @return the check result<br>检查结果
     */
    public boolean accountNameAllowed(String name) {
        if (name.length() > 16) return false;
        return !NAME_PATTERN.asPredicate().test(name);
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

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class FetchProfileTask extends AbstractAction {
        protected Text getTaskName() {
            return translatable("core.oauth.task.fetchProfile.name");
        }

        protected void execute() throws Exception {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                    .uri(apiAccessor.getMinecraftProfileUrl())
                    .header(tokenHeader())
                    .retry(5)
                    .sendAndReadJson(MinecraftProfileRequestModel.class);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class RefreshAccountTask extends AbstractAction {
        protected void execute() throws Exception {
            TokenResponseModel model;
            // TODO Refresh with RefreshToken
            {
                setState(TaskState.<Void>builder()
                        .totalStage(5)
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
                        .retry(5)
                        .sendAndReadJson(TokenResponseModel.class);
            }
            // TODO Update refresh token
            {
                setState(TaskState.<Void>builder()
                        .totalStage(5)
                        .currentStage(2)
                        .message(translatable("core.oauth.refreshAccount.refreshingToken.text"))
                        .build()
                );
                refreshToken = model.getRefreshToken();
            }
            // TODO Fork internal task for fetching AccessToken
            {
                setState(TaskState.<Void>builder()
                        .totalStage(5)
                        .currentStage(3)
                        .message(translatable("core.oauth.refreshAccount.refreshingBase.text"))
                        .build()
                );
                MicrosoftAccount accountNew = apiAccessor.createLoginInternalTask(
                                DeviceCodeConverterModel.builder()
                                        .model(model)
                                        .isDevice(true)
                                        .build())
                        .bindTo(this)
                        .get()
                        .orElseThrow(OAuthXBLNotFoundException::new);
                setAccessToken(accountNew.getAccessToken());
            }
            // TODO Update profile
            {
                setState(TaskState.<Void>builder()
                        .totalStage(5)
                        .currentStage(4)
                        .message(translatable("core.oauth.refreshAccount.updateProfile.text"))
                        .build()
                );
                fetchProfileAsync()
                        .bindTo(this)
                        .get();
            }
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.refreshAccount.name");
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class ValidateAccountTask extends AbstractTask<Boolean> {
        protected Boolean call() {
            try {
                fetchProfileAsync().execute();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.validate.text");
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class DisableAccountCapeTask extends AbstractAction {

        protected void execute() throws Exception {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.DELETE)
                    .uri(apiAccessor.getMinecraftCapeModifyUrl())
                    .header(tokenHeader())
                    .timeout(5000)
                    .reqTimeout(5000)
                    .retry(5)
                    .sendAndReadJson(MinecraftProfileRequestModel.class);
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.disable_cape.text");
        }
    }

    private Map.Entry<String, String> tokenHeader() {
        return new ImmutablePair<>("Authorization", String.format("%s %s", tokenType, getAccessToken()));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class NameChangeableCheckTask extends AbstractTask<Boolean> {
        private String newName;

        protected Boolean call() throws Exception {
            return HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                    .uri(String.format(apiAccessor.getMinecraftNameCheckUrl(), newName))
                    .header(tokenHeader())
                    .timeout(5000)
                    .reqTimeout(5000)
                    .retry(5)
                    .sendAndReadJson(MinecraftNameChangeableRequestModel.class)
                    .getStatus() == MinecraftNameChangeableRequestModel.State.AVAILABLE;
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.name_changeable_check.text");
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class NameChangeTask extends AbstractAction {
        private String newName;

        protected void execute() throws Exception {
            boolean changeable = checkAccountNameChangeableAsync(newName)
                    .bindTo(this)
                    .get()
                    .orElse(false);


            if (changeable) profile = HttpClientWrapper.create(HttpClientWrapper.Method.PUT)
                    .uri(String.format(apiAccessor.getMinecraftNameChangeUrl(), newName))
                    .header(tokenHeader())
                    .timeout(5000)
                    .reqTimeout(5000)
                    .retry(5)
                    .sendAndReadJson(MinecraftProfileRequestModel.class);
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.changeName.text");
        }
    }

    public class AccountNameChangedTimeCheckTask extends AbstractTask<MinecraftNameChangedTimeRequestModel> {

        protected MinecraftNameChangedTimeRequestModel call() throws Exception {
            return HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                    .uri(apiAccessor.getMinecraftNameChangeStateUrl())
                    .header(tokenHeader())
                    .sendAndReadJson(MinecraftNameChangedTimeRequestModel.class);
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.changedTime.text");
        }
    }
}
