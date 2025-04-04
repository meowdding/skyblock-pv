package tech.thatgravyboat.skyblockpv.dfu.fixes

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Unit
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer

object UnbreakableFixer : DataComponentFixer<Unit> {

    private const val TAG = "Unbreakable"

    override fun getComponentType(): DataComponentType<Unit> = DataComponents.UNBREAKABLE
    override fun getData(tag: CompoundTag): Unit? {
        val isUnbreakable = tag.getAndRemoveBoolean(TAG) ?: return null

        return Unit.INSTANCE.takeIf { isUnbreakable }
    }
}
