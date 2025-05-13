package me.owdding.skyblockpv

import com.mojang.brigadier.arguments.StringArgumentType
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import kotlinx.coroutines.runBlocking
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.api.PvAPI
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.config.DevConfig
import me.owdding.skyblockpv.generated.SkyBlockPVExtraData
import me.owdding.skyblockpv.generated.SkyBlockPVModules
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.utils.Utils
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture

@Module
object SkyBlockPv : ModInitializer, Logger by LoggerFactory.getLogger("SkyBlockPv") {
    val mod: ModContainer = FabricLoader.getInstance().getModContainer("skyblockpv").orElseThrow()
    val version: Version = mod.metadata.version
    val configDir: Path = FabricLoader.getInstance().configDir.resolve("skyblockpv")
    val useragent: String = "SkyBlockPV ${version.friendlyString} (${String(Base64.getDecoder().decode("Y29udGFjdEB0aGF0Z3Jhdnlib2F0LnRlY2g="))}})"

    val configurator = Configurator("sbpv")

    val isDevMode get() = McClient.isDev || DevConfig.devMode

    val backgroundTexture = id("buttons/normal")

    override fun onInitialize() {
        Config.register(configurator)
        SkyBlockPVModules.init { SkyBlockAPI.eventBus.register(it) }

        SkyBlockPVExtraData.collected.forEach {
            CompletableFuture.supplyAsync { runBlocking { it.load() } }.exceptionally { throwable ->
                McClient.tell {
                    throw throwable
                }
            }
        }

        runBlocking { PvAPI.authenticate() }
    }

    @Subscription
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.register("pv") {
            callback {
                McClient.setScreenAsync(PvTab.MAIN.create(McClient.self.gameProfile))
            }
            then("player", StringArgumentType.string(), SkyBlockPlayerSuggestionProvider) {
                callback {
                    Utils.openMainScreen(this.getArgument("player", String::class.java))
                }
            }
        }

        event.registerWithCallback("sbpv") {
            McClient.setScreenAsync(ResourcefulConfigScreen.getFactory("sbpv").apply(null))
        }
    }

    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath("skyblock-pv", path)
    fun olympusId(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath("olympus", path)
}
