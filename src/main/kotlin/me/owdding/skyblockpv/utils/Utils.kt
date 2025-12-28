package me.owdding.skyblockpv.utils

import com.google.gson.JsonElement
import com.mojang.authlib.GameProfile
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.toColumn
import me.owdding.lib.rendering.text.TextShader
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.PlayerDbAPI
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs
import me.owdding.skyblockpv.screens.fullscreen.TestFullScreen
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.util.Util
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fStack
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines
import java.nio.file.Files
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.jvm.optionals.getOrNull

object Utils {

    var preferedProfileId: UUID? = null

    val executorPool: ExecutorService = Executors.newFixedThreadPool(12)

    fun getMinecraftItem(id: String): ItemStack = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(id)).defaultInstance

    val onHypixel: Boolean get() = McClient.self.connection?.serverBrand()?.startsWith("Hypixel BungeeCord") == true

    fun <K, V> MutableMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean): MutableMap<K, V> = also { entries.removeIf(predicate) }

    fun fetchGameProfile(username: String, callback: (GameProfile?) -> Unit) {
        if (username.equals(McPlayer.name, true)) {
            McClient.runNextTick { callback(McClient.self.gameProfile) }
            return
        }
        PlayerDbAPI.getProfileAsync(username).thenCompose {
            it?.let { CompletableFuture.completedStage(it) } ?: fetchGameProfile(username).thenApply(Optional<GameProfile>::getOrNull)
        }.thenAccept { profile ->
            McClient.runNextTick {
                profile.takeUnless { it?.id == Util.NIL_UUID }?.let {
                    callback(it)
                }
            }
        }
    }

    fun fetchGameProfile(uuid: UUID, callback: (GameProfile?) -> Unit) {
        if (uuid == McPlayer.uuid) {
            McClient.runNextTick { callback(McClient.self.gameProfile) }
            return
        }
        PlayerDbAPI.getProfileAsync(uuid.toString()).thenCompose {
            it?.let { CompletableFuture.completedStage(it) } ?: fetchGameProfile(uuid).thenApply(Optional<GameProfile>::getOrNull)
        }.thenAccept { profile ->
            McClient.runNextTick {
                profile.takeUnless { it?.id == Util.NIL_UUID }?.let {
                    callback(it)
                }
            }
        }
    }

    fun openMainScreen(name: String) = fetchGameProfile(name) { profile ->
        validateGameProfile(profile) {
            //McClient.setScreenAsync { PvTab.MAIN.create(profile!!) }
            McClient.setScreenAsync { TestFullScreen(profile!!, null) }
        }
    }

    fun validateGameProfile(gameProfile: GameProfile?, onValid: () -> Unit) {
        if (gameProfile == null) {
            (+"messages.player_not_found").sendWithPrefix()
        } else if (!RepoAPI.isInitialized()) {
            ChatUtils.chat(
                """
                §cThe external repo is not initialized.
                §cThis can mean your network is blocking our domain or your internet is not working.
                §cPlease try again later or check your network connection. If the problem persists, please report it on our Discord server with your full log.
                """.trimIndent(),
            )
        } else {
            onValid()
        }
    }

    inline fun <reified T : Any> loadFromRepo(file: String) = runBlocking {
        try {
            SkyBlockPv.mod.findPath("repo/$file.json").orElseThrow()?.let(Files::readString)?.readJson<T>() ?: return@runBlocking null
        } catch (e: Exception) {
            SkyBlockPv.error("Failed to load $file from repo", e)
            null
        }
    }

    internal inline fun <reified T : Any> loadRepoData(file: String): T {
        return loadRepoData<T, T>(file) { it }
    }

    internal inline fun <reified T : Any, B : Any> loadRepoData(file: String, modifier: (Codec<T>) -> Codec<B>): B {
        return loadFromRepo<JsonElement>(file).toDataOrThrow(SkyBlockPVCodecs.getCodec<T>().let(modifier))
    }

    internal inline fun <B : Any> loadRepoData(file: String, supplier: () -> Codec<B>): B {
        return loadFromRepo<JsonElement>(file).toDataOrThrow(supplier())
    }

    internal fun <B : Any> loadRepoData(file: String, codec: Codec<B>): B {
        return loadFromRepo<JsonElement>(file).toDataOrThrow(codec)
    }

    fun text(
        text: String = "",
        color: UInt = PvColors.DARK_GRAY.toUInt(),
        init: MutableComponent.() -> Unit = {},
    ): MutableComponent {
        return Text.of(text) {
            withColor(color.toInt())
            init(this)
        }
    }

    fun whiteText(text: String = "", init: MutableComponent.() -> Unit = {}) = text(text, PvColors.WHITE.toUInt(), init)
    fun MutableComponent.append(text: String, init: MutableComponent.() -> Unit): MutableComponent = this.append(Text.of(text, init))

    fun String.toUuid(): UUID? = runCatching {
        when (this.length) {
            32 -> UUID.fromString("${this.substring(0, 8)}-${this.substring(8, 12)}-${this.substring(12, 16)}-${this.substring(16, 20)}-${this.substring(20)}")
            36 -> UUID.fromString(this)
            else -> null
        }
    }.getOrNull()

    fun UUID.toDashlessString(): String = this.toString().replace("-", "")

    fun String.fixBase64Padding() = replace(Regex("=+$"), "").let { it + "=".repeat((4 - it.length % 4) % 4) }

    fun <I, O> Iterable<I>.mapInParallel(parser: (I) -> O): List<O> = runBlocking {
        this@mapInParallel.map { data -> async { parser.invoke(data) } }.awaitAll()
    }

    fun runAsync(task: () -> Unit): CompletableFuture<Void> = CompletableFuture.runAsync { task() }

    fun Display.withTextShader(shader: TextShader?) = ExtraDisplays.textShader(shader, this)

    fun Component.multiLineDisplay(alignment: Alignment = Alignment.START, shadow: Boolean = false) =
        this.splitLines().map { ExtraDisplays.component(it, shadow = shadow) }.toColumn(alignment = alignment)

    @OptIn(ExperimentalStdlibApi::class)
    fun String.hash(algorithm: String = "SHA-256"): String {
        return MessageDigest.getInstance(algorithm).let {
            it.update(this.toByteArray())
            it.digest()
        }.toHexString()
    }

    fun MutableComponent.withoutShadow(): MutableComponent = this.apply {
        this.shadowColor = null
        this.siblings.filterIsInstance<MutableComponent>().forEach { it.withoutShadow() }
    }

    /** Translatable Component **with** shadow */
    operator fun String.unaryPlus(): MutableComponent = Component.translatable("skyblockpv.$this")

    /** Translatable Component **without** shadow */
    operator fun String.unaryMinus(): MutableComponent = Component.translatable("skyblockpv.$this").withoutShadow()

    fun String.asTranslated(vararg args: Any?, shadow: Boolean = true): MutableComponent =
        Component.translatable("skyblockpv.$this", *args).let { if (!shadow) it.withoutShadow() else it }

    operator fun MutableComponent.plus(other: Component): MutableComponent = this.append(other)

    fun <T : Any> JsonElement?.toData(codec: MapCodec<T>) = this.toData(codec.codec())

    fun <T : Any> JsonElement?.toDataOrThrow(codec: MapCodec<T>): T = this.toDataOrThrow(codec.codec())

    fun Matrix3x2fStack.copy() = Matrix3x2f(this)

    fun openConfig(screen: Screen? = null) = McClient.setScreenAsync { ResourcefulConfigScreen.getFactory(SkyBlockPv.MOD_ID).apply(screen) }

    fun Collection<ItemStack>.filterNotAir() = filterNot { it.isEmpty }
}
