package me.owdding.skyblockpv.screens.windowed.tabs.collection

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.shorten
import me.owdding.skyblockpv.api.CollectionAPI
import me.owdding.skyblockpv.api.CollectionAPI.getProgressToMax
import me.owdding.skyblockpv.api.CollectionAPI.getProgressToNextLevel
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.CollectionItem
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

class CommonCollectionScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null, val category: String) : BaseCollectionScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val width = uiWidth - 20

        val profile = profile
        val filteredCollections = profile.collections?.filter { it.category == category } ?: run {
            Utils.openTab(PvTab.COLLECTION, gameProfile, profile)
            return PvLayouts.empty()
        }
        return PvLayouts.frame {
            display(
                buildList {
                    if (bg.width < 375) {
                        filteredCollections.map(::getElement).map(::listOf).forEach(::add)
                    } else {
                        filteredCollections.chunked(2).forEach { chunk ->
                            val row = buildList {
                                chunk.getOrNull(0)?.let { add(getElement(it)) }
                                chunk.getOrNull(1)?.let { add(getElement(it)) }
                            }
                            add(row)
                        }
                    }
                }.asTable(5).centerIn(width, -1),
            )
        }.asScrollable(uiWidth - 20, uiHeight)
    }

    private fun getElement(col: CollectionItem): Display {
        val collectionEntry = CollectionAPI.getCollectionEntry(col.itemId) ?: return ExtraDisplays.text("Unknown Item")
        val progNext = collectionEntry.getProgressToNextLevel(col.amount)
        val progMaxed = collectionEntry.getProgressToMax(col.amount)
        val isMaxed = progNext.first == collectionEntry.maxTiers && progNext.second == 1.0f

        val progressText = if (isMaxed) {
            ExtraDisplays.text("§2Maxed")
        } else {
            ExtraDisplays.text("${(progNext.second * 100).round()}% to ${progNext.first}")
        }

        val hover = Text.multiline(
            "§l${col.itemStack.hoverName.stripped}",
            "§7Collected: ${col.amount.toFormattedString()}",
            if (!isMaxed) "§7Progress to ${progNext.first}: ${(progNext.second * 100).round()}%" else null,
            "§7Progress to Max: ${if (isMaxed) "§2Maxed" else "${(progMaxed * 100).round()}%"}",
        )

        return Displays.row(
            Displays.item(col.itemStack),
            listOf(
                ExtraDisplays.text(Text.join(col.itemStack.hoverName.stripped, ": ${col.amount.shorten()}")),
                listOf(ExtraDisplays.progress(progNext.second), progressText).toRow(3),
            ).toColumn(1),
            spacing = 5,
            alignment = Alignment.CENTER,
        ).withTooltip(hover)
    }
}
