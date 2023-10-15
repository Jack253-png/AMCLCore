package com.mcreater.amclcore.account.auth

import com.mcreater.amclcore.MetaData.Companion.getLauncherFullVersion
import com.mcreater.amclcore.MetaData.Companion.getLauncherName
import com.mcreater.amclcore.account.AbstractAccount
import com.mcreater.amclcore.account.OfflineAccount
import com.mcreater.amclcore.account.OfflineAccount.Texture.Companion.getTexture
import com.mcreater.amclcore.account.OfflineAccount.Texture.Companion.hasTexture
import com.mcreater.amclcore.util.*
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors


class YggdrasilAuthServer(port: Int) : AbstractHttpServer(port) {
    private val keyPair = KeyUtils.generateKey()
    val accounts: MutableList<OfflineAccount> = Vector()

    init {
        addRoute(Route.create(Pattern.compile("^/$"), Route.IS_GET)) { root() }
        addRoute(Route.create(Pattern.compile("/status"), Route.IS_GET)) { status() }
        addRoute(Route.create(Pattern.compile("/api/profiles/minecraft"), Route.IS_POST)) { profiles(it) }
        addRoute(Route.create(Pattern.compile("/sessionserver/session/minecraft/hasJoined"), Route.IS_GET)) {
            hasJoined(
                it
            )
        }
        addRoute(Route.create(Pattern.compile("/sessionserver/session/minecraft/join"), Route.IS_GET)) { joinServer() }
        addRoute(
            Route.create(
                Pattern.compile("/sessionserver/session/minecraft/profile/(?<uuid>[a-f0-9]{32})"),
                Route.IS_GET
            )
        ) { profile(it) }
        addRoute(Route.create(Pattern.compile("/textures/(?<hash>[a-f0-9]{64})"), Route.IS_GET)) { texture(it) }
    }

    fun findAccount(uuid: UUID): Optional<OfflineAccount> {
        return accounts.stream().filter { (_, uuid1): OfflineAccount -> uuid1 == uuid }.findFirst()
    }

    fun findUUID(account: OfflineAccount): Optional<UUID> {
        return accounts.stream().filter { a: OfflineAccount -> a === account }.findFirst().map(AbstractAccount::uuid)
    }

    fun findByName(name: String?): Optional<OfflineAccount> {
        return accounts.stream().filter { (accountName): OfflineAccount -> accountName == name }.findFirst()
    }

    fun findByUUID(uuid: String): Optional<OfflineAccount> {
        return accounts.stream().filter { (_, uuid1): OfflineAccount ->
            StringUtil.toNoLineUUID(
                uuid1
            ) == uuid
        }.findFirst()
    }

    private fun hasJoined(entry: Map.Entry<IHTTPSession, Matcher>): Response {
        val inp = JsonUtil.map(NetworkUtils.parseQuery(entry.key.queryParameterString))
        if (!inp.containsKey("username")) return badRequest()
        val offlineAccount = findByName(inp["username"])
        return if (offlineAccount.isPresent) ok(
            offlineAccount.get().toSkinResponse(host, keyPair!!.private)
        ) else badRequest()
    }

    private fun profile(entry: Map.Entry<IHTTPSession, Matcher>): Response {
        val uuid = entry.value.group("uuid")
        val account = findByUUID(uuid)
        return if (account.isPresent) ok(account.get().toSkinResponse(host, keyPair!!.private)) else noContent()
    }

    private fun joinServer(): Response {
        return noContent()
    }

    private fun profiles(entry: Map.Entry<IHTTPSession, Matcher>): Response {
        val names: List<String?> = JsonUtil.GSON_PARSER.fromJson<List<*>>(
            IOStreamUtil.readStream(entry.key.inputStream),
            MutableList::class.java
        ) as List<String?>
        return ok(
            accounts.stream()
                .filter { (accountName): OfflineAccount -> names.contains(accountName) }
                .map { obj: OfflineAccount -> obj.toProfile() }
                .collect(Collectors.toList())
        )
    }

    @Throws(IOException::class)
    private fun texture(entry: Map.Entry<IHTTPSession, Matcher>): Response {
        val hash = entry.value.group("hash")
        return if (hasTexture(hash)) {
            val texture = getTexture(hash)
            val data = IOStreamUtil.read(texture!!.source)
            val response =
                newFixedLengthResponse(Response.Status.OK, "image/png", ByteArrayInputStream(data), data.size.toLong())
            response.addHeader("Etag", String.format("\"%s\"", hash))
            response.addHeader("Cache-Control", "max-age=2592000, public")
            response
        } else notFound()
    }

    private fun status(): Response {
        return ok(
            JsonUtil.map(
                JsonUtil.pair("user.count", accounts.size),
                JsonUtil.pair("token.count", 0),
                JsonUtil.pair("pendingAuthentication.count", 0)
            )
        )
    }

    private fun root(): Response {
        return if (keyPair != null) {
            ok(
                JsonUtil.map(
                    JsonUtil.pair<String, Any>("signaturePublickey", KeyUtils.toPEMPublicKey(keyPair.public)),
                    JsonUtil.pair<String, Any>("skinDomains", JsonUtil.createList("127.0.0.1", "localhost")),
                    JsonUtil.pair<String, Any>(
                        "meta", JsonUtil.map(
                            JsonUtil.pair("serverName", getLauncherName()) as Map.Entry<String, Any>?,
                            JsonUtil.pair("implementationName", getLauncherName()) as Map.Entry<String, Any>?,
                            JsonUtil.pair("implementationVersion", getLauncherFullVersion()) as Map.Entry<String, Any>?,
                            JsonUtil.pair("feature.non_email_login", true) as Map.Entry<String, Any>?
                        )
                    )
                )
            )
        } else {
            ok(
                JsonUtil.map(
                    JsonUtil.pair<String, Any>("skinDomains", JsonUtil.createList("127.0.0.1", "localhost")),
                    JsonUtil.pair<String, Any>(
                        "meta", JsonUtil.map(
                            JsonUtil.pair("serverName", getLauncherName()) as Map.Entry<String, Any>?,
                            JsonUtil.pair("implementationName", getLauncherName()) as Map.Entry<String, Any>?,
                            JsonUtil.pair("implementationVersion", getLauncherFullVersion()) as Map.Entry<String, Any>?,
                            JsonUtil.pair("feature.non_email_login", true) as Map.Entry<String, Any>?
                        )
                    )
                )
            )
        }
    }
}
