package com.mcreater.amclcore.account;

import com.google.gson.Gson;
import com.mcreater.amclcore.command.CommandArg;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.concurrent.task.model.BooleanTask;
import com.mcreater.amclcore.concurrent.task.model.EmptyAction;
import com.mcreater.amclcore.concurrent.task.model.ObjectTask;
import com.mcreater.amclcore.concurrent.task.model.RunnableAction;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel;
import com.mcreater.amclcore.util.Hex;
import com.mcreater.amclcore.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.ImageUtil.isValidImage;
import static com.mcreater.amclcore.util.JsonUtil.map;
import static com.mcreater.amclcore.util.JsonUtil.pair;
import static com.mcreater.amclcore.util.StringUtil.toNoLineUUID;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OfflineAccount extends AbstractAccount {
    public static final UUID STEVE = StringUtil.toLineUUID("000000000000300a9d83f9ec9e7fae8e");
    public static final UUID ALEX = StringUtil.toLineUUID("000000000000300a9d83f9ec9e7fae8d");
    @Getter
    @Setter
    private boolean isCustomSkin;
    @Getter
    @Setter
    @NotNull
    private Map<String, Texture> capes = new HashMap<>();
    @Getter
    @Setter
    private String selectedCape;
    @Getter
    @Setter
    private Texture skin;
    @Getter
    @Setter
    private boolean skinSlim = getUuid() == ALEX;

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
            try {
                capes.put(id, Texture.loadTexture(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            try {
                skin = Texture.loadTexture(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    private Texture getCape() {
        return capes.get(selectedCape);
    }

    public Object toSkinResponse(String rootUrl, PrivateKey privateKey) {
        System.out.println(rootUrl);
        Map<String, Object> realTextures = new HashMap<>();
        if (skin != null && skin.getSource() != null) {
            if (isSkinSlim()) {
                realTextures.put("SKIN", map(
                        pair("url", rootUrl + "/textures/" + skin.getHash()),
                        pair("metadata", map(
                                pair("model", "slim")
                        ))));
            } else {
                realTextures.put("SKIN", map(pair("url", rootUrl + "/textures/" + skin.getHash())));
            }
        }
        if (getCape() != null) {
            realTextures.put("CAPE", map(pair("url", rootUrl + "/textures/" + getCape().getHash())));
        }

        Map<String, Object> textureResponse = map(
                pair("timestamp", System.currentTimeMillis()),
                pair("profileId", toNoLineUUID(getUuid())),
                pair("profileName", getAccountName()),
                pair("textures", realTextures)
        );

        return map(
                pair("id", toNoLineUUID(getUuid())),
                pair("name", getAccountName()),
                pair("properties", properties(false, privateKey,
                        pair("textures", new String(
                                Base64.getEncoder().encode(
                                        new Gson().toJson(textureResponse).getBytes(UTF_8)
                                ), UTF_8))))
        );
    }

    @SafeVarargs
    public static List<?> properties(PrivateKey privateKey, Map.Entry<String, String>... entries) {
        return properties(false, privateKey, entries);
    }

    @SafeVarargs
    public static List<?> properties(boolean sign, PrivateKey privateKey, Map.Entry<String, String>... entries) {
        return Stream.of(entries)
                .map(entry -> {
                    LinkedHashMap<String, String> property = new LinkedHashMap<>();
                    property.put("name", entry.getKey());
                    property.put("value", entry.getValue());
                    if (sign) {
                        property.put("signature", sign(entry.getValue(), privateKey));
                    }
                    return property;
                })
                .collect(Collectors.toList());
    }

    private static String sign(String data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(privateKey, new SecureRandom());
            signature.update(data.getBytes(UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    public static class Texture {
        @Getter
        private final File source;
        @Getter
        private final String hash;
        private static final Map<String, Texture> textures = new HashMap<>();

        public static boolean hasTexture(String hash) {
            return textures.containsKey(hash);
        }

        public static Texture getTexture(String hash) {
            return textures.get(hash);
        }

        private static String computeTextureHash(File img) throws IOException {
            return computeTextureHash(Files.newInputStream(img.toPath()));
        }

        private static String computeTextureHash(InputStream img) {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            BufferedImage rd;
            try {
                rd = ImageIO.read(img);
            } catch (IOException e) {
                return "";
            }

            int width = rd.getWidth();
            int height = rd.getHeight();
            byte[] buf = new byte[4096];

            putInt(buf, 0, width);
            putInt(buf, 4, height);
            int pos = 8;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    putInt(buf, pos, rd.getRGB(x, y));
                    if (buf[pos + 0] == 0) {
                        buf[pos + 1] = buf[pos + 2] = buf[pos + 3] = 0;
                    }
                    pos += 4;
                    if (pos == buf.length) {
                        pos = 0;
                        digest.update(buf, 0, buf.length);
                    }
                }
            }
            if (pos > 0) {
                digest.update(buf, 0, pos);
            }

            return Hex.encodeHex(digest.digest());
        }

        private static void putInt(byte[] array, int offset, int x) {
            array[offset + 0] = (byte) (x >> 24 & 0xff);
            array[offset + 1] = (byte) (x >> 16 & 0xff);
            array[offset + 2] = (byte) (x >> 8 & 0xff);
            array[offset + 3] = (byte) (x >> 0 & 0xff);
        }

        public static Texture loadTexture(File image) throws IOException {
            if (image == null) return null;

            String hash = computeTextureHash(image);

            Texture existent = textures.get(hash);
            if (existent != null) {
                return existent;
            }

            Texture texture = new Texture(image, hash);
            existent = textures.putIfAbsent(hash, texture);

            if (existent != null) {
                return existent;
            }
            return texture;
        }
    }
}
