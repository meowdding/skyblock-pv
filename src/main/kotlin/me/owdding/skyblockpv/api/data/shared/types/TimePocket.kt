package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.Lenient
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import net.minecraft.world.item.ItemStack

object TimePocketDataProvider : SharedDataProvider<TimePocketData> {
    override val endpoint: String = "time_pocket"
    override val codec: Codec<TimePocketData> = SkyBlockPvCodecs.TimePocketDataCodec.codec()

    override fun create(): TimePocketData = TODO()
}

@GenerateCodec
data class TimePocketData(
    @Lenient val items: List<ItemStack>
)

