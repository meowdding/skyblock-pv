package tech.thatgravyboat.skyblockpv

import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockpv.api.CollectionAPI
import tech.thatgravyboat.skyblockpv.api.MojangAPI
import tech.thatgravyboat.skyblockpv.api.SkillAPI
import tech.thatgravyboat.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import tech.thatgravyboat.skyblockpv.screens.PvTabs

object SkyBlockPv : ModInitializer {
    override fun onInitialize() {
        val modules = listOf(
            this,
            SkillAPI,
            CollectionAPI,
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
                    runBlocking {
                        val uuid = MojangAPI.getUUID(player)
                        if (uuid == null) {
                            Text.of("§cPlayer could not be found").send()
                        } else {
                            McClient.tell {
                                McClient.setScreen(PvTabs.MAIN.create(McClient.self.minecraftSessionService.fetchProfile(uuid, false)?.profile ?: return@tell))
                            }
                        }
                    }
                }
            }
        }
    }

    fun id(path: String) = ResourceLocation.fromNamespaceAndPath("skyblock-pv", path)
}
