package tech.thatgravyboat.skyblockpv.dfu

import net.minecraft.core.component.DataComponentType
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

interface DataComponentFixer<T> : ItemFixer {
    fun getComponentType(): DataComponentType<T>
    fun getData(compoundTag: CompoundTag): T?

    override fun fix(stack: ItemStack, tag: CompoundTag) {
        stack.set(getComponentType(), getData(tag))
    }
}
