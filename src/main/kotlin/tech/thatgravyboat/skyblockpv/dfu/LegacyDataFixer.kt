package tech.thatgravyboat.skyblockpv.dfu

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.dfu.fixes.*
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.ColorFixer
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.LoreFixer
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.NameFixer

object LegacyDataFixer {

    private val fixers = listOf(
        ItemCountFixer,
        HideFlagsFixer,
        SkullTextureFixer,
        LoreFixer,
        NameFixer,
        ColorFixer,
        UnbreakableFixer,
        EnchantGlintFixer,
        WrittenBookFixer,
        BannerItemFixer,
        ExtraAttributesFixer,
    )

    fun fromTag(tag: Tag): ItemStack? {
        if (tag !is CompoundTag) {
            return ItemStack.EMPTY
        }

        if (tag.isEmpty) return ItemStack.EMPTY

        val item: Item
        try {
            item = FlatteningFixer.getItem(tag)
        } catch (e: NullPointerException) {
            return ItemStack.EMPTY
        }

        val defaultInstance = item.defaultInstance
        fixers.forEach {
            if (it.shouldApply(defaultInstance)) {
                it.fixItem(defaultInstance, tag)
            }
        }

        return defaultInstance
    }
}
