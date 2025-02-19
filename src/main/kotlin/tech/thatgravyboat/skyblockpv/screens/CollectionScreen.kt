package tech.thatgravyboat.skyblockpv.screens

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.ui.UIConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.data.CollectionCategory
import tech.thatgravyboat.skyblockpv.data.CollectionItem
import tech.thatgravyboat.skyblockpv.utils.widgets.ItemWidget
import tech.thatgravyboat.skyblockpv.utils.widgets.SpriteWidget
import java.util.*

private const val ASPECT_RATIO = 9.0 / 16.0

object CollectionScreen : BaseCursorScreen(CommonText.EMPTY) {
    private var currentCategory = CollectionCategory.FARMING

    override fun init() {
        val width = (this.width * 0.6).toInt()
        val height = (width * ASPECT_RATIO).toInt()
        val columnWidth = width / 2 - 20
        val columnHeight = height - 20

        val bg = SpriteWidget()
            .withTexture(UIConstants.BUTTON.enabled)
            .withSize(width, height)

        val uuid = UUID.fromString("b75d7e0a-03d0-4c2a-ae47-809b6b808246")

        CoroutineScope(Dispatchers.IO).launch {
            val screen = this@CollectionScreen

            val profile = ProfileAPI.getProfiles(uuid).find { it.selected } ?: return@launch

            val columnLeft = ListWidget(columnWidth, columnHeight)
            val columnRight = ListWidget(columnWidth, columnHeight)

            val filteredCollections = profile.collections.filter { it.category == currentCategory }

            filteredCollections.chunked(2).forEach { chunk ->
                chunk.getOrNull(0)?.let { columnLeft.add(getElement(it)) }
                chunk.getOrNull(1)?.let { columnRight.add(getElement(it)) }
            }

            val row = LinearLayout.horizontal().spacing(5)
            row.addChild(columnLeft)
            row.addChild(columnRight)

            row.arrangeElements()

            FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)
            FrameLayout.centerInRectangle(row, bg.x, bg.y, bg.width, bg.height)

            bg.visitWidgets(screen::addRenderableOnly)
            row.visitWidgets(screen::addRenderableWidget)
        }
    }

    private fun getElement(col: CollectionItem): BaseWidget {
        val layout = LinearLayout.horizontal().spacing(5)
        layout.addChild(ItemWidget().withItemStack(col.itemStack).withSize(16, 16))
        layout.addChild(Widgets.text(Text.join(col.itemStack?.hoverName ?: col.itemId, ": ${col.amount.toFormattedString()}")))
        layout.arrangeElements()
        return Button().withSize(300, 16).withRenderer { gr, _, _ -> layout.visitWidgets { it.render(gr, 0, 0, 0f) } }
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
