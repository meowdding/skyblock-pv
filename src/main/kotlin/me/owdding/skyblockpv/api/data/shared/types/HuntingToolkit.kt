package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import net.minecraft.world.item.ItemStack

object HuntingToolkitDataProvider : SharedDataProvider<HuntingToolkitData> {
    override val endpoint: String = "hunting_toolkit"
    override val codec: Codec<HuntingToolkitData> = SkyBlockPvCodecs.HuntingToolkitDataCodec.codec()

    override fun create(): HuntingToolkitData = TODO()
}

@GenerateCodec
data class HuntingToolkitData(
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
