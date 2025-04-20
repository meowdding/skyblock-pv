package tech.thatgravyboat.skyblockpv.dfu

import me.owdding.ktmodules.Module
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.getStringOrNull
import tech.thatgravyboat.skyblockapi.utils.extentions.holder
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.dfu.base.BaseItem
import tech.thatgravyboat.skyblockpv.dfu.fixes.*
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.ColorFixer
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.LoreFixer
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.NameFixer

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
