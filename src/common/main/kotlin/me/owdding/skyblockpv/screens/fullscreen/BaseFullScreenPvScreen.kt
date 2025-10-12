package me.owdding.skyblockpv.screens.fullscreen

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.layouts.setPos
import me.owdding.lib.layouts.withPadding
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.components.PvWidgets.getPlayerWidget
import me.owdding.skyblockpv.utils.components.PvWidgets.getStatusButton
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import me.owdding.skyblockpv.widgets.PronounWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.id
import tech.thatgravyboat.skyblockapi.platform.name
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines

abstract class BaseFullScreenPvScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile?) : BasePvScreen(name, gameProfile, profile) {

    override val uiWidth: Int get() = McClient.window.guiScaledWidth
    override val uiHeight: Int get() = McClient.window.guiScaledHeight

    override fun init() {
        val leftSidePadding = 5
        val leftSideWidth = (uiWidth * 0.2).toInt() - leftSidePadding * 2 - 2
        val topBarHeight = 22

        val backgroundWidget =
            Displays.background(ThemeSupport.texture(SkyBlockPv.backgroundTexture), uiWidth - leftSideWidth - 12, uiHeight - topBarHeight - 2).asWidget()

        LayoutFactory.horizontal {
            LayoutFactory.frame(leftSideWidth, uiHeight) {
                widget(
                    LayoutFactory.vertical(3) {
                        widget(getPlayerWidget(leftSideWidth))
                        widget(getStatusButton(leftSideWidth))
                        if (Config.showPronouns) {
                            widget(PronounWidget.getPronounDisplay(gameProfile.id, leftSideWidth).asWidget())
                        }
                    },
                ) {
                    alignVertically(0.4f)
                }
                val search = createSearch(leftSideWidth).withPadding(leftSidePadding)
                widget(createProfileDropdown(leftSideWidth).withPadding(leftSidePadding)) {
                    alignVerticallyBottom()
                    paddingBottom(search.height)
                }
                widget(search) {
                    alignVerticallyBottom()
                }
            }.add()
            display(Displays.background(0xFF202020u, Displays.empty(2, uiHeight)))

            vertical {
                LayoutFactory.frame(uiWidth - leftSideWidth, topBarHeight) {
                    widget(PvWidgets.text("SkyBlockPv v1")) {
                        alignVerticallyMiddle()
                        alignHorizontallyLeft()
                    }
                    // TODO
                    //  categories
                    //  settings button
                    //  other things
                }.add()
                display(Displays.background(0xFF303030u, Displays.empty(uiWidth - leftSideWidth, 2)))
                widget(backgroundWidget)
            }
        }.applyLayout()

        try {
            create(backgroundWidget)
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

class TestFullScreen(gameProfile: GameProfile, profile: SkyBlockProfile?) : BaseFullScreenPvScreen("Test", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        fun Layout.applyLayout() {
            this.setPos(bg.x, bg.y).visitWidgets(this@TestFullScreen::addRenderableWidget)
        }

        LayoutFactory.vertical {
            string("meow")
        }.applyLayout()
    }
}
