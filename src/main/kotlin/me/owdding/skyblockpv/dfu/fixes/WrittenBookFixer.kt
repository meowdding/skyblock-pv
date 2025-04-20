package me.owdding.skyblockpv.dfu.fixes

import me.owdding.skyblockpv.dfu.DataComponentFixer
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.WrittenBookContent

object WrittenBookFixer : DataComponentFixer<WrittenBookContent> {

    override val type: DataComponentType<WrittenBookContent> = DataComponents.WRITTEN_BOOK_CONTENT

    override fun getData(tag: CompoundTag): WrittenBookContent? {
        tag.getAndRemoveInt("generation") ?: return null
        tag.getAndRemoveBoolean("resolved") ?: return null

        return WrittenBookContent.EMPTY
    }
}
