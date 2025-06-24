package me.owdding.skyblockpv.dfu

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.dfu.base.BaseItem
import me.owdding.skyblockpv.dfu.fixes.*
import me.owdding.skyblockpv.dfu.fixes.display.ColorFixer
import me.owdding.skyblockpv.dfu.fixes.display.LoreFixer
import me.owdding.skyblockpv.dfu.fixes.display.NameFixer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.getStringOrNull
import tech.thatgravyboat.skyblockapi.utils.extentions.holder
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson

@Module
object LegacyDataFixer {

    private val fixers = listOf(
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
        FireworkExplosionFixer,
        ItemModelFix,
        RemoveFixer("overrideMeta"),
        RemoveFixer("AttributeModifiers"),
    )

    fun fromTag(tag: Tag): ItemStack? {
        if (tag !is CompoundTag) {
            return ItemStack.EMPTY
        }

        if (tag.isEmpty) return ItemStack.EMPTY

        val base = BaseItem.getBase(tag)

        if (base == null) {
            SkyBlockPv.error("Base item not found for ${tag.getStringOrNull("id")} (${tag.getStringOrNull("Damage")})\n${NbtUtils.prettyPrint(tag)}")
            return null
        }

        val (item, count, builder) = base

        tag.getCompound("tag").ifPresent { tag ->
            fixers.forEach {
                if (!it.canApply(item)) return@forEach
                it.apply(builder, tag)
            }
        }

        val stack = ItemStack(item.holder, count, builder.build())

        if (SkyBlockPv.isDevMode && !tag.isEmpty && !tag.getCompoundOrEmpty("tag").isEmpty) {
            SkyBlockPv.warn(
                """
            Item tag is not empty after applying fixers for ${stack.getData(DataTypes.ID)}:
            ${NbtUtils.prettyPrint(tag)}
            ${stack.toJson(ItemStack.CODEC)}
            """.trimIndent(),
            )
        }

        return stack
    }
}
