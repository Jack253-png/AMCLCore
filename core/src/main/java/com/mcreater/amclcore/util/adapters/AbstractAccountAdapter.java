package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.account.MicrosoftAccount;
import com.mcreater.amclcore.account.OfflineAccount;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel;
import com.mcreater.amclcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AbstractAccountAdapter extends TypeAdapter<AbstractAccount> {
    public static final AbstractAccountAdapter INSTANCE = new AbstractAccountAdapter();

    private AbstractAccountAdapter() {
    }

    public void write(JsonWriter out, AbstractAccount value) throws IOException {
        if (value == null) return;
        if (value.isMicrosoftAccount()) {
            out.beginObject()
                    .name("type").value(1)
                    .name("access_token").value(value.toMicrosoftAccount().getAccessToken())
                    .name("refresh_token").value(value.toMicrosoftAccount().getRefreshToken())
                    .name("token_type").value(value.toMicrosoftAccount().getTokenType())
                    .endObject();
        } else if (value.isOffLineAccount()) {
            out.beginObject()
                    .name("type").value(0)
                    .name("name").value(value.toOffLineAccount().getAccountName())
                    .name("uuid").value(value.toOffLineAccount().getUuid().toString())
                    .endObject();
        } else {
            out.beginObject()
                    .name("type").value(-1)
                    .name("name").value(value.getAccountName())
                    .name("uuid").value(value.getUuid().toString())
                    .endObject();
        }
    }

    public AbstractAccount read(JsonReader in) throws IOException {
        JsonUtil.JsonToMapProcessor processor = new JsonUtil.JsonToMapProcessor(in);
        do {
            processor.process();
        }
        while (processor.processable());
        JsonUtil.MappedJson mappedJson = processor.getProcessedContent();
        int type = mappedJson.tryGetInteger(-1, "type");
        switch (type) {
            default:
            case -1:
                // empty account
                return new AbstractAccount(mappedJson.tryGetString("name"), UUID.fromString(mappedJson.tryGetString("uuid")), mappedJson.tryGetString("uuid")) {
                    public AbstractAction refreshAsync() {
                        return null;
                    }

                    public AbstractAction fetchProfileAsync() {
                        return null;
                    }

                    public AbstractTask<Boolean> validateAccountAsync() {
                        return null;
                    }

                    public AbstractAction disableAccountCapeAsync() {
                        return null;
                    }

                    public AbstractAction enableAccountCapeAsync(String id) {
                        return null;
                    }

                    public AbstractTask<Boolean> checkAccountNameChangeableAsync(@NotNull String newName) {
                        return null;
                    }

                    public AbstractAction changeAccountNameAsync(@NotNull String newName) {
                        return null;
                    }

                    public AbstractTask<MinecraftNameChangedTimeRequestModel> checkAccountNameChangedTimeAsync() {
                        return null;
                    }

                    public boolean accountNameAllowed(String name) {
                        return false;
                    }

                    public AbstractAction resetSkinAsync() {
                        return null;
                    }

                    public AbstractAction uploadSkinAsync(File file, boolean isSlim) {
                        return null;
                    }
                };
            case 0:
                return OfflineAccount.create(
                        mappedJson.tryGetString("name"),
                        UUID.fromString(mappedJson.tryGetString("uuid"))
                );
            case 1:
                return MicrosoftAccount.create(
                        mappedJson.tryGetString("access_token"),
                        mappedJson.tryGetString("refresh_token"),
                        mappedJson.tryGetString("token_type")
                );
        }
    }
}
