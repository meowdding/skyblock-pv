package tech.thatgravyboat.skyblockpv

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockpv.api.CollectionAPI
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.SkillAPI
import tech.thatgravyboat.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import tech.thatgravyboat.skyblockpv.data.EssenceData
import tech.thatgravyboat.skyblockpv.data.ForgeTimeData
import tech.thatgravyboat.skyblockpv.screens.PvTabs
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import tech.thatgravyboat.skyblockpv.utils.Utils

object SkyBlockPv : ModInitializer {
    val version = FabricLoader.getInstance().getModContainer("skyblockpv").orElseThrow().metadata.version

    override fun onInitialize() {
        val modules = listOf(
            this,
            ItemAPI,
            SkillAPI,
            CollectionAPI,
            ForgeTimeData,
            EssenceData,
        )

        modules.forEach { SkyBlockAPI.eventBus.register(it) }
    }

    @Subscription
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.register("pv") {
            callback {
                McClient.tell {
                    McClient.setScreen(PvTabs.MAIN.create(McClient.self.gameProfile))
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
                                McClient.setScreen(PvTabs.MAIN.create(profile))
                            }
                        }
                    }
                }
            }
        }
    }

    fun id(path: String) = ResourceLocation.fromNamespaceAndPath("skyblock-pv", path)
}
