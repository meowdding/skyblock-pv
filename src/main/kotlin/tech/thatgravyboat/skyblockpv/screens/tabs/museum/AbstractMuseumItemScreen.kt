package tech.thatgravyboat.skyblockpv.screens.tabs.museum

import com.mojang.authlib.GameProfile
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.LegacyItemStack
import tech.thatgravyboat.skyblockpv.data.museum.MuseumItem
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.TooltipBuilder
import tech.thatgravyboat.skyblockpv.utils.displays.toColumn
import tech.thatgravyboat.skyblockpv.utils.displays.toRow
import tech.thatgravyboat.skyblockpv.utils.tooltipDataComponents

abstract class AbstractMuseumItemScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMuseumScreen(gameProfile, profile) {

    override fun getLayout() = LayoutBuild.frame {
        getMuseumData().asSequence().sortedBy { it.id }.map { repoData ->
            val item = loaded(
                whileLoading = LegacyItemStack.wrap(Items.ORANGE_DYE.defaultInstance),
                onError = LegacyItemStack.wrap(Items.BEDROCK.defaultInstance),
            ) {
                it.items.find { it.id == repoData.id }?.let { museumItem ->
                    if (museumItem.stacks.size > 1) {
                        SkyBlockPv.error("Stack size is ${museumItem.stacks.size} expected 0 or 1, (${repoData.id})")
                    }
                    museumItem.stacks.firstOrNull()
                } ?: run {
                    val defaultInstance = Items.GRAY_DYE.defaultInstance
                    defaultInstance.set(DataComponents.CUSTOM_NAME, ItemAPI.getItemName(repoData.id))
                    defaultInstance.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, tooltipDataComponents))
                    TooltipBuilder().apply {

                    }.lines().map { Text.multiline(it) }.let {  defaultInstance.set(DataComponents.LORE, ItemLore(it, it)) }

                    LegacyItemStack.wrap(defaultInstance)
                }
            }
            Displays.item(item, showTooltip = true)
        }.map { Displays.padding(2, it) }.chunked(54).map { it.toMutableList() }.map {
            it.rightPad(54, Displays.placeholder(20, 20))
            Displays.background(SkyBlockPv.id("inventory/inventory-9x6"), Displays.padding(2, it.chunked(9).map { it.toRow() }.toColumn()))
        }.toList().let { CarouselWidget(it,0, 246) }.let { widget(it) }
    }

    abstract fun getMuseumData(): List<MuseumItem>
}
