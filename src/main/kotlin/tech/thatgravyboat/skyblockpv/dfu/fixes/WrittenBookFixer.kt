package tech.thatgravyboat.skyblockpv.dfu.fixes

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.WrittenBookContent
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer

object WrittenBookFixer : DataComponentFixer<WrittenBookContent> {
    override fun getComponentType(): DataComponentType<WrittenBookContent> = DataComponents.WRITTEN_BOOK_CONTENT

    override fun getData(compoundTag: CompoundTag): WrittenBookContent? {
        compoundTag.getAndRemoveInt("generation") ?: return null
        compoundTag.getAndRemoveBoolean("resolved") ?: return null

        return WrittenBookContent.EMPTY
    }
}
