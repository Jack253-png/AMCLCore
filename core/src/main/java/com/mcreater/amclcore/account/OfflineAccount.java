package com.mcreater.amclcore.account;

import com.mcreater.amclcore.command.CommandArg;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.concurrent.task.model.BooleanTask;
import com.mcreater.amclcore.concurrent.task.model.EmptyAction;
import com.mcreater.amclcore.concurrent.task.model.ObjectTask;
import com.mcreater.amclcore.concurrent.task.model.RunnableAction;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel;
import com.mcreater.amclcore.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.ImageUtil.isValidImage;
import static com.mcreater.amclcore.util.StringUtil.toNoLineUUID;

public class OfflineAccount extends AbstractAccount {
    public static final UUID STEVE = StringUtil.toLineUUID("000000000000300a9d83f9ec9e7fae8e");
    public static final UUID ALEX = StringUtil.toLineUUID("000000000000300a9d83f9ec9e7fae8d");
    @Getter
    @Setter
    private boolean isCustomSkin;
    @Getter
    @Setter
    @NotNull
    private Map<String, File> capes = new HashMap<>();
    @Getter
    @Setter
    private String selectedCape;
    @Getter
    @Setter
    private File skin;
    @Getter
    @Setter
    private boolean skinSlim = getUuid() == ALEX;
    ;

    private OfflineAccount(@NotNull String accountName, @NotNull UUID uuid) {
        super(accountName, uuid, toNoLineUUID(uuid));
    }

    public static OfflineAccount create(@NotNull String accountName, @NotNull UUID uuid) {
        return new OfflineAccount(accountName, uuid);
    }

    public AbstractAction refreshAsync() {
        return EmptyAction.of();
    }

    public AbstractAction fetchProfileAsync() {
        return EmptyAction.of();
    }

    public BooleanTask validateAccountAsync() {
        return BooleanTask.of(true);
    }

    public RunnableAction disableAccountCapeAsync() {
        return RunnableAction.of(() -> selectedCape = null, translatable("core.oauth.task.disable_cape.text"));
    }

    public RunnableAction enableAccountCapeAsync(String id) {
        return RunnableAction.of(() -> {
            if (capes.containsKey(id)) selectedCape = id;
        }, translatable("core.oauth.task.enable_cape.text"));
    }

    public RunnableAction addAccountCapeAsync(String id, File file) {
        return RunnableAction.of(() -> {
            if (!isValidImage(file)) throw new RuntimeException("bad image");
            capes.put(id, file);
        }, translatable("core.account.offline.cape.add"));
    }

    public RunnableAction removeAccountCapeAsync(String id) {
        return RunnableAction.of(() -> {
            capes.remove(id);
            if (Objects.equals(selectedCape, id)) selectedCape = null;
        }, translatable("core.account.offline.cape.add"));
    }

    public BooleanTask checkAccountNameChangeableAsync(@NotNull String newName) {
        return BooleanTask.of(true, translatable("core.oauth.task.name_changeable_check.text"));
    }

    public RunnableAction changeAccountNameAsync(@NotNull String newName) {
        return RunnableAction.of(() -> setAccountName(newName), translatable("core.oauth.task.changeName.text"));
    }

    public AbstractTask<MinecraftNameChangedTimeRequestModel> checkAccountNameChangedTimeAsync() {
        return ObjectTask.of(null);
    }

    public boolean accountNameAllowed(String name) {
        return true;
    }

    public RunnableAction resetSkinAsync() {
        return RunnableAction.of(() -> {
            skin = null;
            skinSlim = getUuid() == ALEX;
        }, translatable("core.oauth.task.disable_cape.text"));
    }

    public RunnableAction uploadSkinAsync(File file, boolean isSlim) {
        return RunnableAction.of(() -> {
            if (!isValidImage(file)) throw new RuntimeException("bad image");
            skin = file;
            skinSlim = isSlim;
        }, translatable("core.oauth.task.upload_skin.text"));
    }

    public List<CommandArg> getAddonArgs() {
        return new Vector<>();
    }

    public RunnableAction preLaunchAsync() {
        return RunnableAction.of(() -> {
        }, translatable("core.game.launch.pre"));
    }
}
