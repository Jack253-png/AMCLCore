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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.StringUtil.toNoLineUUID;

public class OfflineAccount extends AbstractAccount {
    public static final UUID STEVE = StringUtil.toLineUUID("000000000000300a9d83f9ec9e7fae8e");
    public static final UUID ALEX = StringUtil.toLineUUID("000000000000300a9d83f9ec9e7fae8d");

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

    public AbstractAction disableAccountCapeAsync() {
        return EmptyAction.of();
    }

    public AbstractAction enableAccountCapeAsync(String id) {
        return EmptyAction.of();
    }

    public BooleanTask checkAccountNameChangeableAsync(@NotNull String newName) {
        return BooleanTask.of(true, translatable("core.oauth.task.name_changeable_check.text"));
    }

    public AbstractAction changeAccountNameAsync(@NotNull String newName) {
        return RunnableAction.of(() -> setAccountName(newName), translatable("core.oauth.task.changeName.text"));
    }

    public AbstractTask<MinecraftNameChangedTimeRequestModel> checkAccountNameChangedTimeAsync() {
        return ObjectTask.of(null);
    }

    public boolean accountNameAllowed(String name) {
        return true;
    }

    public AbstractAction resetSkinAsync() {
        return EmptyAction.of();
    }

    public AbstractAction uploadSkinAsync(File file, boolean isSlim) {
        return EmptyAction.of();
    }

    public List<CommandArg> getAddonArgs() {
        return new Vector<>();
    }

    public RunnableAction preLaunchAsync() {
        return RunnableAction.of(() -> {
        }, translatable("core.game.launch.pre"));
    }
}
