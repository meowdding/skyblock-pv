package tech.thatgravyboat.skyblockpv

import net.fabricmc.api.ModInitializer
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockpv.screens.MobScreen

object Init : ModInitializer {
    override fun onInitialize() {
        val modules = listOf(
            this,
        )

        modules.forEach { SkyBlockAPI.eventBus.register(it) }
    }

    @Subscription
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.register("pv") {
            callback {
                McClient.tell {
                    McClient.setScreen(MobScreen)
                }
            }
        }
    }
}
