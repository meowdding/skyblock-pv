package tech.thatgravyboat.skyblockpv

import com.mojang.brigadier.arguments.StringArgumentType
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockpv.api.CollectionAPI
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.RemindersAPI
import tech.thatgravyboat.skyblockpv.api.SkillAPI
import tech.thatgravyboat.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import tech.thatgravyboat.skyblockpv.config.Config
import tech.thatgravyboat.skyblockpv.data.EssenceData
import tech.thatgravyboat.skyblockpv.data.ForgeTimeData
import tech.thatgravyboat.skyblockpv.data.FossilTypes
import tech.thatgravyboat.skyblockpv.screens.PvTab
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import tech.thatgravyboat.skyblockpv.utils.Utils

object SkyBlockPv : ModInitializer {
    val mod = FabricLoader.getInstance().getModContainer("skyblockpv").orElseThrow()
    val version = mod.metadata.version

    val configurator = Configurator("sbpv")

    override fun onInitialize() {
        Config.register(configurator)

        val modules = listOf(
            this,
            ItemAPI,
            SkillAPI,
            CollectionAPI,
            ForgeTimeData,
            EssenceData,
            FossilTypes,
            RemindersAPI,
        )

        modules.forEach { SkyBlockAPI.eventBus.register(it) }
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
