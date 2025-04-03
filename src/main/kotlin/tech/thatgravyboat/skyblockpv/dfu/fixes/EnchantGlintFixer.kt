package tech.thatgravyboat.skyblockpv.dfu.fixes

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer

object EnchantGlintFixer : DataComponentFixer<Boolean> {
    private const val TAG = "ench"

    override fun getComponentType(): DataComponentType<Boolean> = DataComponents.ENCHANTMENT_GLINT_OVERRIDE

    override fun getData(compoundTag: CompoundTag): Boolean? {
        compoundTag.getAndRemoveList(TAG) ?: return null

        return true
    }
}
