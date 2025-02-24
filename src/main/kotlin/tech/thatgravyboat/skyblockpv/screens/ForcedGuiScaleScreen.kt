package tech.thatgravyboat.skyblockpv.screens

import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen

interface ForcedGuiScaleScreen {

    companion object {

        @JvmStatic
        var isInForcedScaleGui = false
        var hasForcedGui = false

        @Subscription
        fun onTick(event: TickEvent) {
            isInForcedScaleGui = McScreen.self is ForcedGuiScaleScreen

            if (isInForcedScaleGui != hasForcedGui) {
                hasForcedGui = isInForcedScaleGui
                McClient.self.resizeDisplay()
            }
        }
    }
}
