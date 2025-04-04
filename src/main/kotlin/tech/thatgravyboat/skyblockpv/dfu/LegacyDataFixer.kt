package tech.thatgravyboat.skyblockpv.dfu

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.Logger
import tech.thatgravyboat.skyblockapi.utils.extentions.getStringOrNull
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.config.Config
import tech.thatgravyboat.skyblockpv.dfu.base.BaseItem
import tech.thatgravyboat.skyblockpv.dfu.fixes.*
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.ColorFixer
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.LoreFixer
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.NameFixer

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
        FireworkExplosionFixer
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

        tag.getCompound("tag").ifPresent { tag ->
            fixers.forEach { it.apply(base, tag) }
        }

        if (Config.devMode && !tag.isEmpty && !tag.getCompoundOrEmpty("tag").isEmpty) {
            Logger.warn("""
            Item tag is not empty after applying fixers for ${base.getData(DataTypes.ID)}:
            ${NbtUtils.prettyPrint(tag)}
            ${base.toJson(ItemStack.CODEC)}
            """.trimIndent())
        }

        return base
    }
}
