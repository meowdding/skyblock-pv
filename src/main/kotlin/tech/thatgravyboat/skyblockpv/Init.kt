package tech.thatgravyboat.skyblockpv

import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockpv.api.MojangAPI
import tech.thatgravyboat.skyblockpv.api.SkillAPI
import tech.thatgravyboat.skyblockpv.command.SkyblockPlayerSuggestionProvider
import tech.thatgravyboat.skyblockpv.screens.PvTabs

object Init : ModInitializer {
    override fun onInitialize() {
        val modules = listOf(
            this,
            SkillAPI,
        )

        modules.forEach { SkyBlockAPI.eventBus.register(it) }
    }

    @Subscription
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.register("pv") {
            callback {
                McClient.tell {
                    McClient.setScreen(PvTabs.MAIN.create(McPlayer.self!!.uuid))
                }
            }
            then("player", StringArgumentType.string(), SkyblockPlayerSuggestionProvider) {
                callback {
                    val player = this.getArgument("player", String::class.java)
                    runBlocking {
                        val uuid = MojangAPI.getUUID(player)
                        if (uuid == null) {
                            McPlayer.self?.displayClientMessage(Component.literal("§cPlayer could not be found"), false)
                        } else {
                            McClient.tell {
                                McClient.setScreen(PvTabs.MAIN.create(uuid))
                            }
                        }
                    }
                }
            }
        }
    }

    fun id(path: String) = ResourceLocation.fromNamespaceAndPath("skyblock-pv", path)
}
