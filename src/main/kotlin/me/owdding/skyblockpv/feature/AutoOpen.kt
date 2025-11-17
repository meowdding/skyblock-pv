package me.owdding.skyblockpv.feature

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.screens.fullscreen.TestFullScreen
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Module
object AutoOpen {

    init {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (System.getProperty("sbpv.autoopen")?.lowercase() == "true") { // Add into vm args -Dsbpv.autoopen="true"
                // Todo: better open
                McClient.setScreenAsync { TestFullScreen(McPlayer.self?.gameProfile!!, null) }
            }
        }
    }

}
