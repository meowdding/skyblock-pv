package me.owdding.skyblockpv

import com.mojang.brigadier.arguments.StringArgumentType
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigUI
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import kotlinx.coroutines.runBlocking
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingUpdateChecker
import me.owdding.lib.utils.isMeowddingDev
import me.owdding.skyblockpv.api.PvAPI
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.config.DevConfig
import me.owdding.skyblockpv.config.THEME_RENDERER
import me.owdding.skyblockpv.config.ThemeRenderer
import me.owdding.skyblockpv.generated.SkyBlockPVExtraData
import me.owdding.skyblockpv.generated.SkyBlockPVModules
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.LiteralCommandBuilder
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.url
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture

@Module
object SkyBlockPv : ModInitializer, Logger by LoggerFactory.getLogger("SkyBlockPv") {
    const val MOD_ID: String = "skyblockpv"
    const val RESOURCE_PATH: String = "skyblock-pv"
    val mod: ModContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()
    val configDir: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
    val version: Version = mod.metadata.version
    val useragent: String = "SkyBlockPV ${version.friendlyString} (${String(Base64.getDecoder().decode("Y29udGFjdEB0aGF0Z3Jhdnlib2F0LnRlY2g="))}})"

    private val configurator = Configurator("sbpv")
    val config by lazy { Config.register(configurator) }

    val isDevMode get() = McClient.isDev || DevConfig.devMode
    val isSuperUser by lazy { McPlayer.uuid.isMeowddingDev() }

    val backgroundTexture = id("buttons/normal")

    override fun onInitialize() {
        config // used to instantiate the lazy :3
        ResourcefulConfigUI.registerElementRenderer(THEME_RENDERER, ::ThemeRenderer)

        SkyBlockPVModules.init { SkyBlockAPI.eventBus.register(it) }

        SkyBlockPVExtraData.collected.forEach {
            CompletableFuture.supplyAsync { runBlocking { it.load() } }.exceptionally { throwable ->
                McClient.runNextTick {
                    throw throwable
                }
            }
        }

        MeowddingUpdateChecker("8yqXwFLl", mod) { link, current, new ->
            fun MutableComponent.withLink() = this.apply {
                this.url = link
                this.hover = Text.of(link).withColor(TextColor.GRAY)
            }

            McClient.runNextTick {
                Text.of().send()
                "messages.new_version".asTranslated(current, new).withLink().sendWithPrefix()
                (+"messages.new_version.download").withLink().sendWithPrefix()
                Text.of().send()
            }
        }
        runBlocking { PvAPI.authenticate() }
    }

    @Subscription
    fun onRegisterCommands(event: RegisterCommandsEvent) {

        val pvCommand: (LiteralCommandBuilder.() -> Unit) = {
            callback {
                McClient.setScreenAsync { PvTab.MAIN.create(McClient.self.gameProfile) }
            }
            then("player", StringArgumentType.string(), SkyBlockPlayerSuggestionProvider) {
                callback {
                    Utils.openMainScreen(this.getArgument("player", String::class.java))
                }
            }
        }

        if (!Config.isDisabled) {
            event.register("pv") { pvCommand() }
        }

        event.register("sbpv") {
            then("pv") { pvCommand() }

            thenCallback("version") {
                Text.of("Version: $version").withColor(TextColor.GRAY).sendWithPrefix()
            }

            callback {
                McClient.setScreenAsync { ResourcefulConfigScreen.getFactory("sbpv").apply(null) }
            }
        }
    }

    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(RESOURCE_PATH, path)
    fun olympusId(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath("olympus", path)
}
