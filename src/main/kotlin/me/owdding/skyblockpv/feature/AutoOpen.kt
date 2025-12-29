package me.owdding.skyblockpv.feature

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.screens.fullscreen.BaseFullScreenPvScreen
import me.owdding.skyblockpv.utils.SkyBlockPvDevUtils
import me.owdding.skyblockpv.utils.Utils
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Module
object AutoOpen {

    init {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (SkyBlockPvDevUtils.getBoolean("autoopen")) { // Add into vm args -Dsbpv.autoopen="true"
                if (SkyBlockPvDevUtils.getString("autoopen.name") != null) {
                    Utils.fetchGameProfile(SkyBlockPvDevUtils.getString("autoopen.name")!!) {
                        McClient.setScreenAsync {
                            BaseFullScreenPvScreen(McPlayer.self?.gameProfile!!, null)
                        }
                    }
                    return@register
                }

                McClient.setScreenAsync {
                    BaseFullScreenPvScreen(McPlayer.self?.gameProfile!!, null)
                }
            }
        }
    }

}
