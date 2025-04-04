package tech.thatgravyboat.skyblockpv.dfu.fixes

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.dfu.ItemFixer
import kotlin.jvm.optionals.getOrNull

object CustomPotionEffectsFixer : ItemFixer {

    override fun fix(stack: ItemStack, tag: CompoundTag) {
        val effects = tag.getList("CustomPotionEffects").getOrNull() ?: return
        if (stack.has(DataComponents.POTION_CONTENTS) && effects.size == 1) {
            tag.remove("CustomPotionEffects")
        }
    }
}
