package tech.thatgravyboat.skyblockpv.screens

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.ui.UIConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.data.CollectionCategory
import tech.thatgravyboat.skyblockpv.data.CollectionItem
import tech.thatgravyboat.skyblockpv.utils.displays.Alignment
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import tech.thatgravyboat.skyblockpv.utils.displays.centerIn
import java.util.*

private const val ASPECT_RATIO = 9.0 / 16.0

object CollectionScreen : BaseCursorScreen(CommonText.EMPTY) {
    private var currentCategory = CollectionCategory.MINING

    override fun init() {
        val width = (this.width * 0.6).toInt()
        val height = (width * ASPECT_RATIO).toInt()
        val columnHeight = height - 20

        val bg = Displays.background(UIConstants.BUTTON.enabled, width, height).asWidget()

        val uuid = UUID.fromString("b75d7e0a-03d0-4c2a-ae47-809b6b808246")

        CoroutineScope(Dispatchers.IO).launch {
            val screen = this@CollectionScreen

            val profile = ProfileAPI.getProfiles(uuid).find { it.selected } ?: return@launch

            val scrollable = ListWidget(width, columnHeight)

            val filteredCollections = profile.collections.filter { it.category == currentCategory }

            val table = buildList {
                filteredCollections.chunked(2).forEach { chunk ->
                    val row = buildList {
                        chunk.getOrNull(0)?.let { add(getElement(it)) }
                        chunk.getOrNull(1)?.let { add(getElement(it)) }
                    }
                    add(row)
                }
            }.asTable().centerIn(width, -1).asWidget()

            scrollable.add(table)

            FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)
            FrameLayout.centerInRectangle(scrollable, bg.x, bg.y, bg.width, bg.height)

            bg.visitWidgets(screen::addRenderableOnly)
            scrollable.visitWidgets(screen::addRenderableWidget)
        }
    }

    private fun getElement(col: CollectionItem): Display {
        val display = Displays.row(
            Displays.item(col.itemStack ?: ItemStack.EMPTY),
            Displays.text(Text.join(col.itemStack?.hoverName ?: col.itemId, ": ${col.amount.toFormattedString()}")),
            spacing = 5,
            alignment = Alignment.CENTER,
        )
        return display
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
