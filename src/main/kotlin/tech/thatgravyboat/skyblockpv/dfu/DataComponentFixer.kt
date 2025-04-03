package tech.thatgravyboat.skyblockpv.dfu

import net.minecraft.core.component.DataComponentType
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

interface DataComponentFixer<T> : ItemFixer {
    fun getComponentType(): DataComponentType<T>
    fun getData(compoundTag: CompoundTag): T?

    override fun fixItem(itemStack: ItemStack, compoundTag: CompoundTag) {
        itemStack.set(getComponentType(), compoundTag.getCompound("tag").map(::getData).orElse(null))
    }
}
