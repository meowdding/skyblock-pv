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
import me.owdding.skyblockpv.feature.PartyFinderJoin.getDungeonData
import me.owdding.skyblockpv.generated.SkyBlockPVExtraData
import me.owdding.skyblockpv.generated.SkyBlockPVModules
import me.owdding.skyblockpv.screens.DisplayTest
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.fetchGameProfile
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
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
object SkyBlockPv : ClientModInitializer, Logger by LoggerFactory.getLogger("SkyBlockPv") {
    const val MOD_ID: String = "skyblockpv"
    const val RESOURCE_PATH: String = "skyblock-pv"
    val mod: ModContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()
    val configDir: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
    val version: Version = mod.metadata.version
    val useragent: String = "SkyBlockPV ${version.friendlyString} (${String(Base64.getDecoder().decode("Y29udGFjdEB0aGF0Z3Jhdnlib2F0LnRlY2g="))}})"

    private val configurator = Configurator(MOD_ID)
    val config by lazy { Config.register(configurator) }

    val isDevMode get() = McClient.isDev || DevConfig.devMode
    val isSuperUser by lazy { McPlayer.uuid.isMeowddingDev() }

    val backgroundTexture = id("buttons/normal")

    fun ifDevMode(action: () -> Unit) {
        if (isDevMode) action()
    }

    override fun onInitializeClient() {
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

        // Prioritise over skyblocker pv
        val commandId = id("skyblock_pv_command")
        ClientCommandRegistrationCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, commandId)
        ClientCommandRegistrationCallback.EVENT.register(commandId) { dispatcher, _ ->
            if (!Config.isDisabled) {
                dispatcher.root.children.removeIf { it.name == "pv" }
                dispatcher.register(
                    ClientCommandManager.literal("pv")
                        .then(
                            ClientCommandManager.argument("player", StringArgumentType.string()).suggests(SkyBlockPlayerSuggestionProvider).executes {
                                Utils.openMainScreen(it.getArgument("player", String::class.java))
                                1
                            },
                        ).executes {
                            McClient.setScreenAsync { PvTab.MAIN.create(McClient.self.gameProfile) }
                            1
                        },
                )
            }
        }
    }

    @Subscription
    fun onRegisterCommands(event: RegisterCommandsEvent) {

        event.register("sbpv") {
            then("pv") {
                callback {
                    McClient.setScreenAsync { PvTab.MAIN.create(McClient.self.gameProfile) }
                }
                then("player", StringArgumentType.string(), SkyBlockPlayerSuggestionProvider) {
                    callback {
                        Utils.openMainScreen(this.getArgument("player", String::class.java))
                    }
                }
            }

            thenCallback("displaytest") {
                McClient.setScreenAsync { DisplayTest }
            }

            thenCallback("version") {
                Text.of("Version: $version").withColor(TextColor.GRAY).sendWithPrefix()
            }

            thenCallback("pfjoin player", StringArgumentType.string(), SkyBlockPlayerSuggestionProvider) {
                fetchGameProfile(this.getArgument("player", String::class.java)) { profile ->
                    Utils.validateGameProfile(profile) {
                        getDungeonData(profile!!)
                    }
                }
            }

            callback {
                McClient.setScreenAsync { ResourcefulConfigScreen.getFactory(MOD_ID).apply(null) }
            }
        }
    }

    fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(RESOURCE_PATH, path)
    fun olympusId(path: String): Identifier = Identifier.fromNamespaceAndPath("olympus", path)
}
