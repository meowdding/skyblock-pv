package me.owdding.skyblockpv.dfu.fixes

import me.owdding.skyblockpv.dfu.DataComponentFixer
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Unit

object UnbreakableFixer : DataComponentFixer<Unit> {

    private const val TAG = "Unbreakable"

    override val type: DataComponentType<Unit> = DataComponents.UNBREAKABLE

    override fun getData(tag: CompoundTag): Unit? {
        val isUnbreakable = tag.getAndRemoveBoolean(TAG) ?: return null

        return Unit.INSTANCE.takeIf { isUnbreakable }
    }
}
