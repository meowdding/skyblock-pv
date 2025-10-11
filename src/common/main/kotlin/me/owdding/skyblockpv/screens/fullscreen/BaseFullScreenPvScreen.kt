package me.owdding.skyblockpv.screens.fullscreen

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.layouts.withPadding
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.components.PvWidgets.getPlayerWidget
import net.minecraft.client.gui.layouts.FrameLayout
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.id
import tech.thatgravyboat.skyblockapi.platform.name
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines

abstract class BaseFullScreenPvScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile?) : BasePvScreen(name, gameProfile, profile) {

    override val uiWidth: Int get() = McClient.window.guiScaledWidth
    override val uiHeight: Int get() = McClient.window.guiScaledHeight

    override fun init() {

        LayoutFactory.horizontal {
            val padding = 5
            val leftSideWidth = (uiWidth * 0.2).toInt() - padding * 2

            LayoutFactory.frame(leftSideWidth, uiHeight) {
                widget(getPlayerWidget(leftSideWidth)) {
                    alignVerticallyMiddle()
                }
                widget(createProfileDropdown(leftSideWidth).withPadding(padding)) {
                    alignVerticallyBottom()
                }
            }.add()
            display(Displays.background(0xFF202020u, Displays.empty(2, uiHeight)))
        }.applyLayout(0, 0)

        try {
            // create screen
        } catch (e: Exception) {
            e.printStackTrace()

            val errorWidget = PvLayouts.vertical {
                val text = "widgets.error.stacktrace".asTranslated(
                    name,
                    gameProfile.name,
                    gameProfile.id,
                    profile.id.name,
                    e.javaClass.name,
                    e.message,
                    e.getStackTraceString(7),
                )


                text.splitLines().forEach {
                    widget(PvWidgets.text(it).withCenterAlignment().withSize(uiWidth, 10))
                }
            }
            FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
            errorWidget.applyLayout()
        }
    }
}

class TestFullScreen(gameProfile: GameProfile, profile: SkyBlockProfile?) : BaseFullScreenPvScreen("Test", gameProfile, profile)
