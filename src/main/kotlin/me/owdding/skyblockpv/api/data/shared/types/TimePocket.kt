package me.owdding.skyblockpv.api.data.shared.types

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.Lenient
import net.minecraft.world.item.ItemStack

@GenerateCodec
data class TimePocket(
    @Lenient val items: List<ItemStack>
)

