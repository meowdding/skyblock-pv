package me.owdding.skyblockpv.utils

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.pipelines.RoundedRectanage
import kotlinx.coroutines.runBlocking
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.PlayerDbAPI
import me.owdding.skyblockpv.screens.PvTab
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.SkullBlockEntity
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.nio.file.Files
import java.util.*
import kotlin.jvm.optionals.getOrNull

object Utils {

    fun getMinecraftItem(id: String) = BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(id)).defaultInstance

    var isFetchingGameProfile = false
        private set

    fun GuiGraphics.drawRoundedRec(
        x: Int, y: Int, width: Int, height: Int,
        backgroundColor: Int, borderColor: Int = backgroundColor,
        borderSize: Int = 0, radius: Int = 0,
    ) {
        this.flush()

        val xOffset = this.pose().last().pose().m30()
        val yOffset = this.pose().last().pose().m31()
        pushPop {
            translate(-xOffset, -yOffset, 0f)
            RoundedRectanage.draw(
                this@drawRoundedRec, (x + xOffset).toInt(), (y + yOffset).toInt(), width, height,
                backgroundColor, borderColor, width.coerceAtMost(height) * (radius / 100f), borderSize,
            )
        }
    }

    fun fetchGameProfile(username: String, callback: (GameProfile?) -> Unit) {
        if (isFetchingGameProfile) return
        isFetchingGameProfile = true
        PlayerDbAPI.getUUID(username).takeUnless { it?.id == Util.NIL_UUID }?.let {
            callback(it)
            isFetchingGameProfile = false
        }
            ?: SkullBlockEntity.fetchGameProfile(username).thenAccept { profile ->
                callback(profile.getOrNull())
                isFetchingGameProfile = false
            }
    }

    fun openMainScreen(name: String) = fetchGameProfile(name) { profile ->
        if (profile == null) {
            ChatUtils.chat("§cPlayer could not be found")
        } else {
            McClient.tell { McClient.setScreen(PvTab.MAIN.create(profile)) }
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

    fun text(
        text: String = "",
        color: UInt = 0x555555u,
        init: MutableComponent.() -> Unit = {},
    ): MutableComponent {
        return Text.of(text) {
            withColor(color.toInt())
            init(this)
        }
    }
    fun whiteText(text: String = "", init: MutableComponent.() -> Unit = {}) = text(text, 0xFFFFFFu, init)
    fun MutableComponent.append(text: String, init: MutableComponent.() -> Unit): MutableComponent = this.append(Text.of(text, init))

    fun UUID.toDashlessString(): String = this.toString().replace("-", "")

    fun String.fixBase64Padding() = replace(Regex("=+$"), "").let { it + "=".repeat((4 - it.length % 4) % 4) }
}
