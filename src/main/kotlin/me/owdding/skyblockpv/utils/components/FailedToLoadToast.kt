package me.owdding.skyblockpv.utils.components

import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.ProfileId
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastManager
import tech.thatgravyboat.skyblockapi.helpers.McClient

data class FailedToLoadToast(
    private val display: Display,
    private val time: Int
) : Toast {

    private var removalTime: Long = -1

    override fun width(): Int = display.getWidth()
    override fun height(): Int = display.getHeight()
    override fun getToken(): Any = Toast.NO_TOKEN
    override fun getWantedVisibility() = if (this.removalTime <= System.currentTimeMillis()) Toast.Visibility.HIDE else Toast.Visibility.SHOW
    override fun update(toastManager: ToastManager, visibilityTime: Long) {}
    override fun render(graphics: GuiGraphics, ignore1: Font, ignore2: Long) {
        if (removalTime == -1L) {
            removalTime = System.currentTimeMillis() + this.time
        }

        this.display.render(graphics)
    }

    companion object {

        private const val RESEND_TIME = 10 * 60 * 1000L

        private val failedToLoadForUsers = mutableMapOf<ProfileId, Long>()

        fun add(
            profile: SkyBlockProfile,
            display: Display,
            time: Int = 5000,
        ) {
            val lastTime = failedToLoadForUsers[profile.id]
            if (lastTime != null && System.currentTimeMillis() - lastTime < RESEND_TIME) return
            failedToLoadForUsers[profile.id] = System.currentTimeMillis()
            McClient.toasts.addToast(FailedToLoadToast(display, time))
        }
    }
}
