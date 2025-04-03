package tech.thatgravyboat.skyblockpv.dfu.fixes

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.dfu.ItemFixer

object ItemCountFixer : ItemFixer {

    private const val TAG = "Count"

    override fun fixItem(itemStack: ItemStack, compoundTag: CompoundTag) {
        itemStack.count = compoundTag.getAndRemoveInt(TAG)?: 1
    }
}
