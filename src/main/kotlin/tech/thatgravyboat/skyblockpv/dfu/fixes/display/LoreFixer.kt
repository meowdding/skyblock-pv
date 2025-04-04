package tech.thatgravyboat.skyblockpv.dfu.fixes.display

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.ItemLore
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer
import kotlin.jvm.optionals.getOrNull

object LoreFixer : DataComponentFixer<ItemLore> {
    private const val DISPLAY_TAG = "display"
    private const val TAG = "Lore"

    override val type: DataComponentType<ItemLore> = DataComponents.LORE

    override fun getData(tag: CompoundTag): ItemLore? {
        val display = tag.getCompound(DISPLAY_TAG).getOrNull() ?: return null
        val loreTag = display.getAndRemoveList(TAG) ?: return null
        tag.removeIfEmpty(DISPLAY_TAG)

        val lore = loreTag.mapNotNull { it.asString().getOrNull() }.map { Text.of(it) }
        return ItemLore(lore, lore)
    }

}
