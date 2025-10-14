package me.owdding.skyblockpv.screens.fullscreen

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.UIIcons
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.layouts.setPos
import me.owdding.lib.layouts.withPadding
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.components.PvWidgets.centerIn
import me.owdding.skyblockpv.utils.components.PvWidgets.getPlayerWidget
import me.owdding.skyblockpv.utils.components.PvWidgets.getStatusButton
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import me.owdding.skyblockpv.widgets.PronounWidget
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.id
import tech.thatgravyboat.skyblockapi.platform.name
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines

abstract class BaseFullScreenPvScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile?) : BasePvScreen(name, gameProfile, profile) {

    override val uiWidth: Int get() = McClient.window.guiScaledWidth
    override val uiHeight: Int get() = McClient.window.guiScaledHeight

    abstract fun create(x: Int, y: Int, width: Int, height: Int)

    override fun init() {
        val leftSidePadding = 5
        val leftSideWidth = (uiWidth * 0.2).toInt()
        val leftSideWidthInnerWidth = leftSideWidth - leftSidePadding * 2 - 2
        val topBarHeight = 24

        val backgroundWidget = Displays.background(
            ThemeSupport.texture(SkyBlockPv.backgroundTexture),
            uiWidth - leftSideWidth - 2,
            uiHeight - topBarHeight - 3,
        ).asWidget()

        LayoutFactory.horizontal {
            LayoutFactory.frame(leftSideWidthInnerWidth, uiHeight) {
                widget(
                    LayoutFactory.vertical(3) {
                        widget(getPlayerWidget(leftSideWidthInnerWidth))
                        widget(getStatusButton(leftSideWidthInnerWidth))
                        if (Config.showPronouns) {
                            widget(PronounWidget.getPronounDisplay(gameProfile.id, leftSideWidthInnerWidth).asWidget())
                        }
                    },
                ) {
                    alignVertically(0.4f)
                }
                val search = createSearch(leftSideWidthInnerWidth).withPadding(leftSidePadding)
                widget(createProfileDropdown(leftSideWidthInnerWidth).withPadding(leftSidePadding)) {
                    alignVerticallyBottom()
                    paddingBottom(search.height)
                }
                widget(search) {
                    alignVerticallyBottom()
                }
            }.add()
            display(Displays.background(0xFF202020u, Displays.empty(2, uiHeight)))

            vertical {
                LayoutFactory.frame(uiWidth - leftSideWidthInnerWidth - 13, topBarHeight) {
                    widget(PvWidgets.text("SkyBlockPv v1")) {
                        alignVerticallyMiddle()
                        alignHorizontallyLeft()
                    }
                    // TODO
                    //  categories
                    //  other things

                    LayoutFactory.horizontal(3) {
                        Widgets.button {
                            it.withRenderer(
                                WidgetRenderers.layered(
                                    WidgetRenderers.sprite(ExtraConstants.BUTTON_DARK),
                                    WidgetRenderers.icon<Button>(UIIcons.PENCIL).withColor(MinecraftColors.WHITE).withPadding(2),
                                ),
                            )
                            it.withTexture(null)
                            it.withSize(22, 22)
                            it.withCallback { Utils.openConfig(this@BaseFullScreenPvScreen) }
                        }.add()
                    }.add {
                        alignVerticallyMiddle()
                        alignHorizontallyRight()
                    }
                }.add()
                display(Displays.background(0xFF303030u, Displays.empty(uiWidth - leftSideWidthInnerWidth, 2)))
                widget(backgroundWidget)
            }
        }.applyLayout()

        try {
            create(backgroundWidget.x + 5, backgroundWidget.y + 5, backgroundWidget.width - 10, backgroundWidget.height - 10)
        } catch (e: Exception) {
            e.printStackTrace()

            PvLayouts.vertical {
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
            }.centerIn(backgroundWidget).applyLayout()
        }
    }
}

class TestFullScreen(gameProfile: GameProfile, profile: SkyBlockProfile?) : BaseFullScreenPvScreen("Test", gameProfile, profile) {
    override fun create(x: Int, y: Int, width: Int, height: Int) {
        fun Layout.applyLayout() {
            this.setPos(x, y).visitWidgets(this@TestFullScreen::addRenderableWidget)
        }

        LayoutFactory.vertical {
            display(Displays.background(0x80000000u, Displays.empty(10, 10)))
        }.applyLayout()
    }
}
