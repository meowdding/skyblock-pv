package me.owdding.skyblockpv.dfu.fixes

import me.owdding.skyblockpv.dfu.DataComponentFixer
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.CustomData

object ExtraAttributesFixer : DataComponentFixer<CustomData> {

    override val type: DataComponentType<CustomData> = DataComponents.CUSTOM_DATA

    override fun getData(tag: CompoundTag): CustomData? {
        val extraAttributes = tag.getAndRemoveCompound("ExtraAttributes") ?: return null
        return CustomData.of(extraAttributes)
    }
}
