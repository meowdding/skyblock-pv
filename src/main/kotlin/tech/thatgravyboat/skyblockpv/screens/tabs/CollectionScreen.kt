package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.base.ListWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.data.SkyblockProfile
import tech.thatgravyboat.skyblockpv.data.CollectionCategory
import tech.thatgravyboat.skyblockpv.data.CollectionItem
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.displays.*

class CollectionScreen(gameProfile: GameProfile, profile: SkyblockProfile? = null) : BasePvScreen("COLLECTION", gameProfile, profile) {

    private var currentCategory = CollectionCategory.MINING

    override fun create(bg: DisplayWidget) {
        val columnHeight = uiHeight - 20

        val profile = profile ?: return
        val scrollable = ListWidget(uiWidth, columnHeight)
        val filteredCollections = profile.collections.filter { it.category == currentCategory }
        val table = buildList {
            filteredCollections.chunked(2).forEach { chunk ->
                val row = buildList {
                    chunk.getOrNull(0)?.let { add(getElement(it)) }
                    chunk.getOrNull(1)?.let { add(getElement(it)) }
                }
                add(row)
            }
        }.asTable(5).centerIn(uiWidth, -1).asWidget()

        scrollable.add(table)

        FrameLayout.centerInRectangle(scrollable, bg.x, bg.y, bg.width, bg.height)

        scrollable.visitWidgets(this::addRenderableWidget)
    }

    private fun getElement(col: CollectionItem): Display {
        val display = Displays.row(
            Displays.item(col.itemStack ?: ItemStack.EMPTY),
            listOf(
                Displays.text(Text.join(col.itemStack?.hoverName ?: col.itemId, ": ${col.amount.toFormattedString()}")),
                listOf(Displays.progress(0.5f), Displays.text("0.5% VII")).toRow(3),
            ).toColumn(1),
            spacing = 5,
            alignment = Alignment.CENTER,
        )
        return display
    }
}
