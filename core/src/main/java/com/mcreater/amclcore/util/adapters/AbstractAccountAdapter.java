package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.account.MicrosoftAccount;
import com.mcreater.amclcore.account.OfflineAccount;
import com.mcreater.amclcore.command.CommandArg;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel;
import com.mcreater.amclcore.util.JsonUtil;
import com.mcreater.amclcore.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AbstractAccountAdapter extends TypeAdapter<AbstractAccount> {
    public static final AbstractAccountAdapter INSTANCE = new AbstractAccountAdapter();

    private AbstractAccountAdapter() {
    }

    public void write(JsonWriter out, AbstractAccount value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        if (value.isMicrosoftAccount()) {
            out.beginObject()
                    .name("type").value(1)
                    .name("access_token").value(value.toMicrosoftAccount().getAccessToken())
                    .name("refresh_token").value(value.toMicrosoftAccount().getRefreshToken())
                    .name("token_type").value(value.toMicrosoftAccount().getTokenType())
                    .name("name").value(value.toMicrosoftAccount().getAccountName())
                    .name("uuid").value(StringUtil.toNoLineUUID(value.toMicrosoftAccount().getUuid()))
                    .endObject();
        } else if (value.isOffLineAccount()) {
            out.beginObject()
                    .name("type").value(0)
                    .name("name").value(value.toOffLineAccount().getAccountName())
                    .name("uuid").value(value.toOffLineAccount().getUuid().toString())
                    .name("custom").value(value.toOffLineAccount().isCustomSkin());
            out.name("custom_skin").beginObject()
                    .name("file").value(
                            Optional.ofNullable(value.toOffLineAccount().getSkin())
                                    .map(OfflineAccount.Texture::getSource)
                                    .map(File::getAbsolutePath)
                                    .orElse(null))
                    .name("isSlim").value(value.toOffLineAccount().isSkinSlim())
                    .endObject()
                    .name("custom_cape").beginObject()
                    .name("selected").value(value.toOffLineAccount().getSelectedCape())
                    .name("capes").beginObject();

            value.toOffLineAccount().getCapes().forEach((s, file) -> {
                try {
                    out.name(s).value(file.getSource().getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            out.endObject().endObject().endObject();
        } else {
            out.beginObject()
                    .name("type").value(-1)
                    .name("name").value(value.getAccountName())
                    .name("uuid").value(value.getUuid().toString())
                    .endObject();
        }
    }

    public AbstractAccount read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
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

                    public List<CommandArg> getAddonArgs() {
                        return null;
                    }

                    public AbstractAction preLaunchAsync() {
                        return null;
                    }
                };
            case 0:
                OfflineAccount ofaccount = OfflineAccount.create(
                        mappedJson.tryGetString("name"),
                        UUID.fromString(mappedJson.tryGetString("uuid"))
                );
                ofaccount.setCustomSkin(mappedJson.tryGetBoolean("custom"));
                mappedJson.tryGetBoolean("custom");
                ofaccount.setSelectedCape(mappedJson.tryGetString("custom_cape", "selected"));
                Map<String, OfflineAccount.Texture> capes = new HashMap<>();
                mappedJson.tryGetMap("custom_cape", "capes").forEach((s, o) -> {
                    try {
                        capes.put(s, OfflineAccount.Texture.loadTexture(new File(o.toString())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                ofaccount.setCapes(capes);

                try {
                    ofaccount.setSkin(OfflineAccount.Texture.loadTexture(new File(mappedJson.tryGetString("custom_skin", "file"))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ofaccount.setSkinSlim(mappedJson.tryGetBoolean("custom_skin", "isSlim"));

                return ofaccount;
            case 1:
                MicrosoftAccount msaccount = MicrosoftAccount.create(
                        mappedJson.tryGetString("access_token"),
                        mappedJson.tryGetString("refresh_token"),
                        mappedJson.tryGetString("token_type")
                );
                msaccount.initBasicProfile(
                        mappedJson.tryGetString("name"),
                        StringUtil.toLineUUID(mappedJson.tryGetString("uuid"))
                );
                return msaccount;
        }
    }
}
