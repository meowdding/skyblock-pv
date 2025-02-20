package tech.thatgravyboat.skyblockpv.screens.tabs

import earth.terrarium.olympus.client.components.base.ListWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.data.CollectionCategory
import tech.thatgravyboat.skyblockpv.data.CollectionItem
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.displays.Alignment
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import tech.thatgravyboat.skyblockpv.utils.displays.centerIn
import java.util.*

class CollectionScreen(player: UUID) : BasePvScreen("COLLECTION", player) {
    private var currentCategory = CollectionCategory.MINING

    override suspend fun create(width: Int, height: Int, bg: DisplayWidget) {
        val columnHeight = height - 20

        val profile = ProfileAPI.getProfiles(uuid).find { it.selected } ?: return
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

        FrameLayout.centerInRectangle(scrollable, bg.x, bg.y, bg.width, bg.height)

        scrollable.visitWidgets(this@CollectionScreen::addRenderableWidget)
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
}
