package tech.thatgravyboat.skyblockpv.dfu.fixes

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.WrittenBookContent
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer

object WrittenBookFixer : DataComponentFixer<WrittenBookContent> {

    override val type: DataComponentType<WrittenBookContent> = DataComponents.WRITTEN_BOOK_CONTENT

    override fun getData(tag: CompoundTag): WrittenBookContent? {
        tag.getAndRemoveInt("generation") ?: return null
        tag.getAndRemoveBoolean("resolved") ?: return null

        return WrittenBookContent.EMPTY
    }
}
