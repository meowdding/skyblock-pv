package me.owdding.skyblockpv.api.data.shared.types

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import net.minecraft.world.item.ItemStack

@GenerateCodec
data class HuntingToolkit(
    @FieldName("axe") val axe: ItemStack?,
    @FieldName("black_hole") val blackHole: ItemStack?,
    @FieldName("lasso") val lasso: ItemStack?,
    @FieldName("fishing_net") val fishingNet: ItemStack?,
    @FieldName("trap0") val trap0: ItemStack?,
    @FieldName("trap1") val trap1: ItemStack?,
    @FieldName("trap2") val trap2: ItemStack?,
    @FieldName("trap3") val trap3: ItemStack?,
    @FieldName("trap4") val trap4: ItemStack?,
)
