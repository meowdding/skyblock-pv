package tech.thatgravyboat.skyblockpv

import com.mojang.brigadier.arguments.StringArgumentType
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.generated.SkyBlockPVModules
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import tech.thatgravyboat.skyblockpv.config.Config
import tech.thatgravyboat.skyblockpv.config.DevConfig
import tech.thatgravyboat.skyblockpv.screens.PvTab
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import tech.thatgravyboat.skyblockpv.utils.Utils

@Module
object SkyBlockPv : ModInitializer, Logger by LoggerFactory.getLogger("SkyBlockPv") {
    val mod = FabricLoader.getInstance().getModContainer("skyblockpv").orElseThrow()
    val version = mod.metadata.version
    val configDir = FabricLoader.getInstance().configDir.resolve("skyblockpv")

    val configurator = Configurator("sbpv")

    val isDevMode get() = McClient.isDev || DevConfig.devMode

    override fun onInitialize() {
        Config.register(configurator)
        SkyBlockPVModules.init { SkyBlockAPI.eventBus.register(it) }
    }

    @Subscription
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.register("pv") {
            callback {
                McClient.tell {
                    McClient.setScreen(PvTab.MAIN.create(McClient.self.gameProfile))
                }
            }
            then("player", StringArgumentType.string(), SkyBlockPlayerSuggestionProvider) {
                callback {
                    val player = this.getArgument("player", String::class.java)
                    Utils.fetchGameProfile(player) { profile ->
                        if (profile == null) {
                            ChatUtils.chat("§cPlayer could not be found")
                        } else {
                            McClient.tell {
                                McClient.setScreen(PvTab.MAIN.create(profile))
                            }
                        }
                    }
                }
            }
        }

        event.register("sbpv") {
            callback {
                McClient.tell {
                    McClient.setScreen(ResourcefulConfigScreen.getFactory("sbpv").apply(null))
                }
            }
        }
    }

    fun id(path: String) = ResourceLocation.fromNamespaceAndPath("skyblock-pv", path)
}
