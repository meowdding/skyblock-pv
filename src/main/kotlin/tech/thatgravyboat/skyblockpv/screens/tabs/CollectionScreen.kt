package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.CollectionAPI
import tech.thatgravyboat.skyblockpv.api.CollectionAPI.getIconFromCollectionType
import tech.thatgravyboat.skyblockpv.api.CollectionAPI.getProgressToNextLevel
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.CollectionItem
import tech.thatgravyboat.skyblockpv.data.SortedEntry.Companion.sortToCollectionCategoryOrder
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.displays.*

class CollectionScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("COLLECTION", gameProfile, profile) {

    private var currentCategory = "FARMING"

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

        addCategories(bg)
    }

    private fun addCategories(bg: DisplayWidget) {
        val categories = profile!!.collections.map { it.category }.distinct().sortToCollectionCategoryOrder()
        val buttonRow = LinearLayout.horizontal().spacing(2)
        categories.forEach { category ->
            val selected = category == currentCategory
            val button = Button()
            button.setSize(20, 22)
            if (selected) {
                button.withTexture(ExtraConstants.TAB_TOP_SELECTED)
            } else {
                button.withCallback {
                    currentCategory = category
                    this.rebuildWidgets()
                }
                button.withTexture(ExtraConstants.TAB_TOP)
            }
            button.withRenderer(
                WidgetRenderers.padded(
                    4, 0, 0, 0,
                    WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(getIconFromCollectionType(category), ctx.x, ctx.y) },
                ),
            )
            button.withTooltip(Text.of(category))
            buttonRow.addChild(button)
        }
        buttonRow.arrangeElements()
        buttonRow.setPosition(bg.x + 20, bg.y - buttonRow.height)
        buttonRow.visitWidgets(this::addRenderableWidget)
    }

    private fun getElement(col: CollectionItem): Display {
        val collectionEntry = CollectionAPI.getCollectionEntry(col.itemId) ?: return Displays.text("Unknown Item")
        val prog = collectionEntry.getProgressToNextLevel(col.amount)

        val progressText = if (prog.first == collectionEntry.maxTiers && prog.second == 1.0f) {
            Displays.text("§2Maxed")
        } else {
            Displays.text("${(prog.second * 100).round()}% to ${prog.first}")
        }

        val display = Displays.row(
            Displays.item(col.itemStack ?: ItemStack.EMPTY),
            listOf(
                Displays.text(Text.join(col.itemStack?.hoverName ?: col.itemId, ": ${col.amount.toFormattedString()}")),
                listOf(Displays.progress(prog.second), progressText).toRow(3),
            ).toColumn(1),
            spacing = 5,
            alignment = Alignment.CENTER,
        )
        return display
    }
}
