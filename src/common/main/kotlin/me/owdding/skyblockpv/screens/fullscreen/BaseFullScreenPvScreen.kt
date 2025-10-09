package me.owdding.skyblockpv.screens.fullscreen

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.platform.InputConstants
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.floor
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.layouts.withPadding
import me.owdding.lib.utils.keys
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.utils.FakePlayer
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.id
import tech.thatgravyboat.skyblockapi.platform.name
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines

abstract class BaseFullScreenPvScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile?) : BasePvScreen(name, gameProfile, profile) {
    private val rightClick = keys {
        withButton(InputConstants.MOUSE_BUTTON_RIGHT)
    }
    private var cachedX = 0.0F
    private var cachedY = 0.0F

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

    private fun getPlayerWidget(width: Int): AbstractWidget {
        val height = (width * 1.1).toInt()
        val armor = (if (isProfileInitialized()) profile.inventory?.armorItems?.inventory else null) ?: List(4) { ItemStack.EMPTY }
        val name = if (isProfileInitialized()) {
            val skyblockLvl = profile.skyBlockLevel.first
            val skyblockLvlColor = tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI.getLevelColor(skyblockLvl)
            Text.join("§8[", Text.of("$skyblockLvl").withColor(skyblockLvlColor), "§8] §f", gameProfile.name)
        } else Text.of(gameProfile.name)
        val fakePlayer = FakePlayer(gameProfile, name, armor)
        val nakedFakePlayer = FakePlayer(gameProfile, name)
        return Displays.background(ThemeSupport.texture(SkyBlockPv.id("buttons/disabled")), width, height).asWidget().withRenderer { gr, ctx, _ ->
            val isHovered = ctx.mouseX in ctx.x..(ctx.x + width) && ctx.mouseY in ctx.y..(ctx.y + height)
            val eyesX = (ctx.mouseX - ctx.x).toFloat().takeIf { ctx.mouseX >= 0 }?.also { cachedX = it } ?: cachedX
            val eyesY = (ctx.mouseY - ctx.y).toFloat().takeIf { ctx.mouseY >= 0 }?.also { cachedY = it } ?: cachedY
            gr.pushPop {
                Displays.entity(
                    if (rightClick.isDown() && isHovered) {
                        nakedFakePlayer
                    } else {
                        fakePlayer
                    },
                    width,
                    height,
                    (width / 3f).floor(),
                    eyesX, eyesY,
                ).render(gr, ctx.x, ctx.y + height / 10)
            }
        }
    }
}

class TestFullScreen(gameProfile: GameProfile, profile: SkyBlockProfile?) : BaseFullScreenPvScreen("Test", gameProfile, profile)
