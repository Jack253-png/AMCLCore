package com.mcreater.amclcore.account;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.command.CommandArg;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.concurrent.task.model.RunnableAction;
import com.mcreater.amclcore.exceptions.oauth.OAuthMinecraftNameChangeNotAllowedException;
import com.mcreater.amclcore.exceptions.oauth.OAuthMinecraftNameConflictException;
import com.mcreater.amclcore.exceptions.oauth.OAuthXBLNotFoundException;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel;
import com.mcreater.amclcore.model.oauth.MinecraftRequestModel;
import com.mcreater.amclcore.model.oauth.TokenResponseModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftEnableCapeResponseModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangeableRequestModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;
import com.mcreater.amclcore.util.HttpClientWrapper;
import com.mcreater.amclcore.util.date.StandardDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.FunctionUtil.genSelfFunction;
import static com.mcreater.amclcore.util.ImageUtil.isValidImage;
import static com.mcreater.amclcore.util.JsonUtil.createPair;
import static com.mcreater.amclcore.util.date.DateUtil.dateBetween;
import static com.mcreater.amclcore.util.date.DateUtil.toDate;

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

            public String getMinecraftSkinResetUrl() {
                throw new UnsupportedOperationException("not implemented yet");
            }

            public String getMinecraftSkinModifyUrl() {
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

        String getMinecraftSkinResetUrl();

        String getMinecraftSkinModifyUrl();
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
    private boolean isBasicInited = false;

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

    public void initBasicProfile(String name, UUID uuid) {
        if (profile == null && name != null && uuid != null) profile = MinecraftProfileRequestModel.builder()
                .id(uuid)
                .name(name)
                .build();
        isBasicInited = true;
    }

    public boolean needFetchProfile() {
        return profile == null || isBasicInited;
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
     * create enable selected cape task<br>
     * 创建启用选定披风任务
     *
     * @param id the cape index<br>披风序列号
     * @return the created task<br>被创建的任务
     */
    public EnableAccountCapeTask enableAccountCapeAsync(String id) {
        return new EnableAccountCapeTask(id);
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

    /**
     * create reset skin task<br>
     * 创建重置皮肤任务
     *
     * @return the created task<br>被创建的任务
     */
    public ResetSkinTask resetSkinAsync() {
        return new ResetSkinTask();
    }

    /**
     * create upload skin task<br>
     * 创建上传皮肤任务
     *
     * @return the created task<br>被创建的任务
     */
    public UploadSkinTask uploadSkinAsync(File file, boolean isSlim) {
        return new UploadSkinTask(file, isSlim);
    }

    public List<CommandArg> getAddonArgs() {
        return new Vector<>();
    }

    public RunnableAction preLaunchAsync() {
        return RunnableAction.of(() -> {
        }, translatable("core.game.launch.pre"));
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

            Optional<MinecraftNameChangedTimeRequestModel> model = checkAccountNameChangedTimeAsync()
                    .bindTo(this)
                    .get();

            Date curr = Date.from(
                    model.map(MinecraftNameChangedTimeRequestModel::getChangedAt)
                            .orElse(StandardDate.DEFAULT)
                            .convert()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
            Date next = Date.from(
                    curr.toInstant()
                            .plus(30, ChronoUnit.DAYS)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
            Date system = Date.from(
                    Instant.now()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );

            if (!changeable) throw new OAuthMinecraftNameConflictException();
            if (!model.map(MinecraftNameChangedTimeRequestModel::isNameChangeAllowed).orElse(false))
                throw new OAuthMinecraftNameChangeNotAllowedException(
                        toDate(curr),
                        toDate(next),
                        dateBetween(next, system));

            profile = HttpClientWrapper.create(HttpClientWrapper.Method.PUT)
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
                    .timeout(5000)
                    .reqTimeout(5000)
                    .retry(5)
                    .sendAndReadJson(MinecraftNameChangedTimeRequestModel.class);
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.changedTime.text");
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class EnableAccountCapeTask extends AbstractAction {
        private String index;

        protected void execute() throws Exception {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.PUT)
                    .uri(apiAccessor.getMinecraftCapeModifyUrl())
                    .header(tokenHeader())
                    .entityJson(
                            MinecraftEnableCapeResponseModel.builder()
                                    .capeId(index)
                                    .build()
                    )
                    .timeout(5000)
                    .reqTimeout(5000)
                    .retry(5)
                    .sendAndReadJson(MinecraftProfileRequestModel.class);
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.enable_cape.text");
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class ResetSkinTask extends AbstractAction {

        protected void execute() throws Exception {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.DELETE)
                    .uri(apiAccessor.getMinecraftSkinResetUrl())
                    .header(tokenHeader())
                    .timeout(5000)
                    .reqTimeout(5000)
                    .retry(5)
                    .sendAndReadJson(MinecraftProfileRequestModel.class);
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.reset_skin.text");
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class UploadSkinTask extends AbstractAction {
        private File file;
        private boolean isSlim;

        protected void execute() throws Exception {
            if (!isValidImage(file)) throw new IOException("bad image");
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                    .uri(apiAccessor.getMinecraftSkinModifyUrl())
                    .header(tokenHeader())
                    .entity(
                            MultipartEntityBuilder.create()
                                    .addBinaryBody("file", file)
                                    .addTextBody("variant", isSlim ? "slim" : "classic")
                                    .build()
                    )
                    .timeout(5000)
                    .reqTimeout(5000)
                    .retry(5)
                    .sendAndReadJson(MinecraftProfileRequestModel.class);
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.upload_skin.text");
        }
    }
}
