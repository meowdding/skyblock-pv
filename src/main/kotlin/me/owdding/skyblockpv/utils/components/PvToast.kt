package me.owdding.skyblockpv.utils.components

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.ProfileId
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.multiLineDisplay
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastManager
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

data class PvToast(
    private val display: Display,
    private val time: Int,
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

        fun addFailedToLoadDataToast() {
            val display = Displays.background(
                ThemeSupport.texture(SkyBlockPv.id("buttons/normal")),
                Displays.padding(
                    5,
                    Displays.column(
                        Displays.text("§lFailed to load SbPv repo data!", color = { TextColor.RED.toUInt() }, shadow = false),
                        Displays.text("This might cause unexpected behaviour,", color = { TextColor.RED.toUInt() }, shadow = false),
                        Displays.text("try restarting your game.", color = { TextColor.RED.toUInt() }, shadow = false),
                        Displays.empty(height = 2),
                        Displays.text("In case the issue persists please", color = { TextColor.RED.toUInt() }, shadow = false),
                        Displays.text("report it on the discord.", color = { TextColor.RED.toUInt() }, shadow = false),
                    ),
                ),
            )
            McClient.toasts.addToast(PvToast(display, 15000))
        }

        fun addFailedToLoadForUsers(
            profile: SkyBlockProfile,
            disabledTabs: List<PvTab>,
            time: Int = 5000,
        ) {
            val lastTime = failedToLoadForUsers[profile.id]
            if (lastTime != null && System.currentTimeMillis() - lastTime < RESEND_TIME) return
            failedToLoadForUsers[profile.id] = System.currentTimeMillis()
            val display = Displays.background(
                ThemeSupport.texture(SkyBlockPv.id("buttons/normal")),
                Displays.padding(
                    5,
                    Displays.column(
                        ExtraDisplays.component(+"messages.toast.disabled_tabs", shadow = false),
                        "messages.toast.disabled_tabs.explanation".asTranslated(disabledTabs.joinToString(", ") { it.name.toTitleCase() })
                            .multiLineDisplay(shadow = false),
                    ),
                ),
            )
            McClient.toasts.addToast(PvToast(display, time))
        }

        fun addSocialsCopiedToast(text: String, time: Int = 5000) {
            val display = Displays.background(
                ThemeSupport.texture(SkyBlockPv.id("buttons/normal")),
                Displays.padding(
                    5,
                    ExtraDisplays.component("messages.toast.socials_copied".asTranslated(text), shadow = false),
                ),
            )
            McClient.toasts.addToast(PvToast(display, time))
        }
    }
}
