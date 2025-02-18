package tech.thatgravyboat.skyblockpv.screens

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.Widgets
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.utils.text.CommonText

abstract class ProfileViewerScreen : BaseCursorScreen(CommonText.EMPTY) {
    override fun init() {

    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
