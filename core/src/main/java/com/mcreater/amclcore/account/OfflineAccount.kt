package com.mcreater.amclcore.account

import com.google.gson.Gson
import com.mcreater.amclcore.account.auth.YggdrasilAuthServer
import com.mcreater.amclcore.command.CommandArg
import com.mcreater.amclcore.command.CommandArg.Companion.create
import com.mcreater.amclcore.concurrent.task.AbstractAction
import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.concurrent.task.model.BooleanTask
import com.mcreater.amclcore.concurrent.task.model.EmptyAction
import com.mcreater.amclcore.concurrent.task.model.ObjectTask
import com.mcreater.amclcore.concurrent.task.model.RunnableAction
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel
import com.mcreater.amclcore.resources.ResourceFetcher
import com.mcreater.amclcore.util.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.*
import java.util.*
import java.util.stream.Collectors
import javax.imageio.ImageIO
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


data class OfflineAccount(
    override var accountName: String?,
    override var uuid: UUID?,
    var isCustomSkin: Boolean = false,
    var capes: MutableMap<String?, Texture?> = HashMap(),
    var selectedCape: String? = null,
    var skin: Texture? = null,
    var skinSlim: Boolean = uuid === ALEX,
    var server: YggdrasilAuthServer? = null
) : AbstractAccount(accountName!!, uuid!!, StringUtil.toNoLineUUID(uuid)) {
    private constructor(accountName: String, uuid: UUID) : this(
        accountName,
        uuid,
        false,
        HashMap(),
        null,
        null,
        uuid === ALEX,
        null
    )

    override fun refreshAsync(): AbstractAction {
        return EmptyAction.of()
    }

    override fun fetchProfileAsync(): AbstractAction {
        return EmptyAction.of()
    }

    override fun validateAccountAsync(): BooleanTask {
        return BooleanTask.of(true)
    }

    override fun disableAccountCapeAsync(): RunnableAction {
        return RunnableAction.of({ selectedCape = null }, translatable("core.oauth.task.disable_cape.text"))
    }

    override fun enableAccountCapeAsync(id: String?): RunnableAction {
        return RunnableAction.of(
            { if (capes.containsKey(id)) selectedCape = id },
            translatable("core.oauth.task.enable_cape.text")
        )
    }

    fun addAccountCapeAsync(id: String?, file: File?): RunnableAction {
        return RunnableAction.of({
            if (!ImageUtil.isValidImage(file)) throw RuntimeException("bad image")
            try {
                capes[id] = Texture.loadTexture(file)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }, translatable("core.account.offline.cape.add"))
    }

    fun removeAccountCapeAsync(id: String?): RunnableAction {
        return RunnableAction.of({
            capes.remove(id)
            if (selectedCape == id) selectedCape = null
        }, translatable("core.account.offline.cape.add"))
    }

    override fun checkAccountNameChangeableAsync(newName: String): BooleanTask {
        return BooleanTask.of(true, translatable("core.oauth.task.name_changeable_check.text"))
    }

    override fun changeAccountNameAsync(newName: String): RunnableAction {
        return RunnableAction.of({ accountName = newName }, translatable("core.oauth.task.changeName.text"))
    }

    override fun checkAccountNameChangedTimeAsync(): AbstractTask<MinecraftNameChangedTimeRequestModel?>? {
        return ObjectTask.of(null)
    }

    override fun accountNameAllowed(name: String?): Boolean {
        return true
    }

    override fun resetSkinAsync(): RunnableAction {
        return RunnableAction.of({
            skin = null
            skinSlim = uuid === ALEX
        }, translatable("core.oauth.task.disable_cape.text"))
    }

    override fun uploadSkinAsync(file: File?, isSlim: Boolean): RunnableAction {
        return RunnableAction.of({
            if (!ImageUtil.isValidImage(file)) throw RuntimeException("bad image")
            skin = try {
                Texture.loadTexture(file)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            skinSlim = isSlim
        }, translatable("core.oauth.task.upload_skin.text"))
    }

    override val addonArgs: List<CommandArg?>
        get() = object : Vector<CommandArg>() {
            init {
                if (hasCustom()) {
                    val file = File("authlib-injector.jar")
                    if (!file.exists()) {
                        try {
                            IOStreamUtil.write(file, ResourceFetcher["authlib-injector.jar"])
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }
                    }
                    add(create("-javaagent:" + file.absolutePath + "=" + server!!.host))
                }
            }
        }

    override fun preLaunchAsync(): RunnableAction {
        return RunnableAction.of({
            if (hasCustom()) {
                var port = 2
                while (port <= 65535) {
                    try {
                        val server1 = YggdrasilAuthServer(port)
                        server1.start()
                        server = server1
                        server1.accounts.add(this)
                        return@of
                    } catch (e: Exception) {
                        e.printStackTrace()
                        port++
                    }
                }
                throw RuntimeException(IOException())
            }
        }, translatable("core.game.launch.pre"))
    }

    private val cape: Texture?
        get() = capes[selectedCape]

    private fun hasCustom(): Boolean {
        return skin != null || capes.isNotEmpty()
    }

    fun toSkinResponse(rootUrl: String, privateKey: PrivateKey?): Any {
        println(rootUrl)
        val realTextures: MutableMap<String, Any> = HashMap()
        if (skin?.source != null) {
            if (skinSlim) {
                realTextures["SKIN"] = JsonUtil.map(
                    JsonUtil.pair<String, Any>("url", rootUrl + "/textures/" + skin!!.hash),
                    JsonUtil.pair<String, Any>(
                        "metadata", JsonUtil.map(
                            JsonUtil.pair("model", "slim")
                        )
                    )
                )
            } else {
                realTextures["SKIN"] = JsonUtil.map(
                    JsonUtil.pair(
                        "url",
                        rootUrl + "/textures/" + skin!!.hash
                    )
                )
            }
        }
        if (cape != null) {
            realTextures["CAPE"] =
                JsonUtil.map(
                    JsonUtil.pair(
                        "url",
                        rootUrl + "/textures/" + cape!!.hash
                    )
                )
        }
        val textureResponse = JsonUtil.map(
            JsonUtil.pair<String, Any>("timestamp", System.currentTimeMillis()),
            JsonUtil.pair<String, Any>("profileId", StringUtil.toNoLineUUID(uuid)),
            JsonUtil.pair<String, Any>("profileName", accountName),
            JsonUtil.pair<String, Any>("textures", realTextures)
        )
        return JsonUtil.map(
            JsonUtil.pair<String, Any>("id", StringUtil.toNoLineUUID(uuid)),
            JsonUtil.pair<String, Any>("name", accountName),
            JsonUtil.pair<String, Any>(
                "properties", properties(
                    false, privateKey,
                    JsonUtil.pair(
                        "textures", String(
                            Base64.getEncoder().encode(
                                Gson().toJson(textureResponse).toByteArray(StandardCharsets.UTF_8)
                            ), StandardCharsets.UTF_8
                        )
                    )
                )
            )
        )
    }

    data class Texture(
        val source: File? = null,
        val hash: String? = null
    ) {
        companion object {
            @JvmStatic
            private val textures: MutableMap<String, Texture> = HashMap()

            @JvmStatic
            fun hasTexture(hash: String): Boolean {
                return textures.containsKey(hash)
            }

            @JvmStatic
            fun getTexture(hash: String): Texture? {
                return textures[hash]
            }

            @JvmStatic
            @Throws(IOException::class)
            private fun computeTextureHash(img: File): String {
                return computeTextureHash(Files.newInputStream(img.toPath()))
            }

            @JvmStatic
            private fun computeTextureHash(img: InputStream): String {
                val digest: MessageDigest = try {
                    MessageDigest.getInstance("SHA-256")
                } catch (e: NoSuchAlgorithmException) {
                    throw RuntimeException(e)
                }
                val rd: BufferedImage = try {
                    ImageIO.read(img)
                } catch (e: IOException) {
                    return ""
                }
                val width = rd.width
                val height = rd.height
                val buf = ByteArray(4096)
                putInt(buf, 0, width)
                putInt(buf, 4, height)
                var pos = 8
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        putInt(buf, pos, rd.getRGB(x, y))
                        if (buf[pos + 0].toInt() == 0) {
                            buf[pos + 3] = 0
                            buf[pos + 2] = buf[pos + 3]
                            buf[pos + 1] = buf[pos + 2]
                        }
                        pos += 4
                        if (pos == buf.size) {
                            pos = 0
                            digest.update(buf, 0, buf.size)
                        }
                    }
                }
                if (pos > 0) {
                    digest.update(buf, 0, pos)
                }
                return Hex.encodeHex(digest.digest())
            }

            @JvmStatic
            private fun putInt(array: ByteArray, offset: Int, x: Int) {
                array[offset + 0] = (x shr 24 and 0xff).toByte()
                array[offset + 1] = (x shr 16 and 0xff).toByte()
                array[offset + 2] = (x shr 8 and 0xff).toByte()
                array[offset + 3] = (x shr 0 and 0xff).toByte()
            }

            @JvmStatic
            @Throws(IOException::class)
            fun loadTexture(image: File?): Texture? {
                if (image == null) return null
                val hash = computeTextureHash(image)
                var existent = textures[hash]
                if (existent != null) {
                    return existent
                }
                val texture = Texture(image, hash)
                existent = textures.putIfAbsent(hash, texture)
                return existent ?: texture
            }
        }
    }

    companion object {
        @JvmStatic
        val STEVE = StringUtil.toLineUUID("000000000000300a9d83f9ec9e7fae8e")

        @JvmStatic
        val ALEX = StringUtil.toLineUUID("000000000000300a9d83f9ec9e7fae8d")

        @JvmStatic
        fun create(accountName: String, uuid: UUID): OfflineAccount {
            return OfflineAccount(accountName, uuid)
        }

        @JvmStatic
        fun properties(sign: Boolean, privateKey: PrivateKey?, vararg entries: Map.Entry<String?, String?>): List<*> {
            return listOf(*entries)
                .stream()
                .map { (key, value): Map.Entry<String?, String?> ->
                    val property =
                        LinkedHashMap<String, String>()
                    property["name"] = key!!
                    property["value"] = value!!
                    if (sign) property["signature"] = sign(value, privateKey)
                    property
                }
                .collect(Collectors.toList())
        }

        @JvmStatic
        private fun sign(data: String, privateKey: PrivateKey?): String {
            return try {
                val signature = Signature.getInstance("SHA1withRSA")
                signature.initSign(privateKey, SecureRandom())
                signature.update(data.toByteArray(StandardCharsets.UTF_8))
                Base64.getEncoder().encodeToString(signature.sign())
            } catch (e: GeneralSecurityException) {
                throw RuntimeException(e)
            }
        }
    }
}
