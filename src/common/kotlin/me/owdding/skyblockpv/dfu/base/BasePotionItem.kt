package me.owdding.skyblockpv.dfu.base

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.core.Holder
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions

object BasePotionItem {

    private val potions = Int2ObjectOpenHashMap<Holder<Potion>>()

    init {
        potions[0] = Potions.WATER
        potions[1] = Potions.REGENERATION
        potions[2] = Potions.SWIFTNESS
        potions[3] = Potions.FIRE_RESISTANCE
        potions[4] = Potions.POISON
        potions[5] = Potions.HEALING
        potions[6] = Potions.NIGHT_VISION
        //potions[7] = null
        potions[8] = Potions.WEAKNESS
        potions[9] = Potions.STRENGTH
        potions[10] = Potions.SLOWNESS
        potions[11] = Potions.LEAPING
        potions[12] = Potions.HARMING
        potions[13] = Potions.WATER_BREATHING
        potions[14] = Potions.INVISIBILITY
        //potions[15] = null
        potions[16] = Potions.AWKWARD
        potions[17] = Potions.REGENERATION
        potions[18] = Potions.SWIFTNESS
        potions[19] = Potions.FIRE_RESISTANCE
        potions[20] = Potions.POISON
        potions[21] = Potions.HEALING
        potions[22] = Potions.NIGHT_VISION
        //potions[23] = null
        potions[24] = Potions.WEAKNESS
        potions[25] = Potions.STRENGTH
        potions[26] = Potions.SLOWNESS
        potions[27] = Potions.LEAPING
        potions[28] = Potions.HARMING
        potions[29] = Potions.WATER_BREATHING
        potions[30] = Potions.INVISIBILITY
        //potions[31] = null
        potions[32] = Potions.THICK
        potions[33] = Potions.STRONG_REGENERATION
        potions[34] = Potions.STRONG_SWIFTNESS
        potions[35] = Potions.FIRE_RESISTANCE
        potions[36] = Potions.STRONG_POISON
        potions[37] = Potions.STRONG_HEALING
        potions[38] = Potions.NIGHT_VISION
        //potions[39] = null
        potions[40] = Potions.WEAKNESS
        potions[41] = Potions.STRONG_STRENGTH
        potions[42] = Potions.SLOWNESS
        potions[43] = Potions.STRONG_LEAPING
        potions[44] = Potions.STRONG_HARMING
        potions[45] = Potions.WATER_BREATHING
        potions[46] = Potions.INVISIBILITY
        //potions[47] = null
        //potions[48] = null
        potions[49] = Potions.STRONG_REGENERATION
        potions[50] = Potions.STRONG_SWIFTNESS
        potions[51] = Potions.FIRE_RESISTANCE
        potions[52] = Potions.STRONG_POISON
        potions[53] = Potions.STRONG_HEALING
        potions[54] = Potions.NIGHT_VISION
        //potions[55] = null
        potions[56] = Potions.WEAKNESS
        potions[57] = Potions.STRONG_STRENGTH
        potions[58] = Potions.SLOWNESS
        potions[59] = Potions.STRONG_LEAPING
        potions[60] = Potions.STRONG_HARMING
        potions[61] = Potions.WATER_BREATHING
        potions[62] = Potions.INVISIBILITY
        //potions[63] = null
        potions[64] = Potions.MUNDANE
        potions[65] = Potions.LONG_REGENERATION
        potions[66] = Potions.LONG_SWIFTNESS
        potions[67] = Potions.LONG_FIRE_RESISTANCE
        potions[68] = Potions.LONG_POISON
        potions[69] = Potions.HEALING
        potions[70] = Potions.LONG_NIGHT_VISION
        //potions[71] = null
        potions[72] = Potions.LONG_WEAKNESS
        potions[73] = Potions.LONG_STRENGTH
        potions[74] = Potions.LONG_SLOWNESS
        potions[75] = Potions.LONG_LEAPING
        potions[76] = Potions.HARMING
        potions[77] = Potions.LONG_WATER_BREATHING
        potions[78] = Potions.LONG_INVISIBILITY
        //potions[79] = null
        potions[80] = Potions.AWKWARD
        potions[81] = Potions.LONG_REGENERATION
        potions[82] = Potions.LONG_SWIFTNESS
        potions[83] = Potions.LONG_FIRE_RESISTANCE
        potions[84] = Potions.LONG_POISON
        potions[85] = Potions.HEALING
        potions[86] = Potions.LONG_NIGHT_VISION
        //potions[87] = null
        potions[88] = Potions.LONG_WEAKNESS
        potions[89] = Potions.LONG_STRENGTH
        potions[90] = Potions.LONG_SLOWNESS
        potions[91] = Potions.LONG_LEAPING
        potions[92] = Potions.HARMING
        potions[93] = Potions.LONG_WATER_BREATHING
        potions[94] = Potions.LONG_INVISIBILITY
        //potions[95] = null
        potions[96] = Potions.THICK
        potions[97] = Potions.REGENERATION
        potions[98] = Potions.SWIFTNESS
        potions[99] = Potions.LONG_FIRE_RESISTANCE
        potions[100] = Potions.POISON
        potions[101] = Potions.STRONG_HEALING
        potions[102] = Potions.LONG_NIGHT_VISION
        //potions[103] = null
        potions[104] = Potions.LONG_WEAKNESS
        potions[105] = Potions.STRENGTH
        potions[106] = Potions.LONG_SLOWNESS
        potions[107] = Potions.LEAPING
        potions[108] = Potions.STRONG_HARMING
        potions[109] = Potions.LONG_WATER_BREATHING
        potions[110] = Potions.LONG_INVISIBILITY
        //potions[111] = null
        //potions[112] = null
        potions[113] = Potions.REGENERATION
        potions[114] = Potions.SWIFTNESS
        potions[115] = Potions.LONG_FIRE_RESISTANCE
        potions[116] = Potions.POISON
        potions[117] = Potions.STRONG_HEALING
        potions[118] = Potions.LONG_NIGHT_VISION
        //potions[119] = null
        potions[120] = Potions.LONG_WEAKNESS
        potions[121] = Potions.STRENGTH
        potions[122] = Potions.LONG_SLOWNESS
        potions[123] = Potions.LEAPING
        potions[124] = Potions.STRONG_HARMING
        potions[125] = Potions.LONG_WATER_BREATHING
        potions[126] = Potions.LONG_INVISIBILITY
        //potions[127] = null

        potions.defaultReturnValue(Potions.WATER)
    }

    private val cache = Int2ObjectOpenHashMap<PotionContents>()

    fun getBase(meta: Int, tag: CompoundTag?): Pair<Item, PotionContents> {
        tag?.remove("CustomPotionEffects")

        val item = if (meta.and(16384) == 16384) Items.SPLASH_POTION else Items.POTION
        val contents = cache.getOrPut(meta.and(127)) { PotionContents(potions[meta.and(127)] ?: Potions.WATER) }
        return item to contents
    }
}
