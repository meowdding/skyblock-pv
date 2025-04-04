package tech.thatgravyboat.skyblockpv.dfu.fixes

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer

object EnchantGlintFixer : DataComponentFixer<Boolean> {
    private const val TAG = "ench"

    override val type: DataComponentType<Boolean> = DataComponents.ENCHANTMENT_GLINT_OVERRIDE

    override fun getData(tag: CompoundTag): Boolean? {
        tag.getAndRemoveList(TAG) ?: return null

        return true
    }
}
