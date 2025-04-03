package tech.thatgravyboat.skyblockpv.dfu.fixes

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.CustomData
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer

object ExtraAttributesFixer : DataComponentFixer<CustomData> {
    override fun getComponentType(): DataComponentType<CustomData> = DataComponents.CUSTOM_DATA

    override fun getData(compoundTag: CompoundTag): CustomData? {
        val extraAttributes = compoundTag.getAndRemoveCompound("ExtraAttributes") ?: return null
        return CustomData.of(extraAttributes)
    }
}
