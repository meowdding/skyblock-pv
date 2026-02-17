package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.Lenient
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import net.minecraft.world.item.ItemStack

object MiscForagingDataProvider : SharedDataProvider<MiscForagingData> {
    override val endpoint: String = "foraging"
    override val codec: Codec<MiscForagingData> = SkyBlockPvCodecs.MiscForagingDataCodec.codec()

    override fun create(): MiscForagingData = TODO()
}

@GenerateCodec
data class MiscForagingData(
    @FieldName("hunting_exp") val huntingExp: String,
    @FieldName("hunting_axe_item") @Lenient val huntingAxeItem: ItemStack?,
    @FieldName("temple_buff_end") val templeBuffEnd: Long,
    @FieldName("beacon_tier") val beaconTier: Int,
    @FieldName("forest_essence") val forestEssence: Int,
    @FieldName("agatha_level_cap") val agathaLevelCap: Int,
    @FieldName("agatha_power") val agathaPower: Int,
    @FieldName("fig_fortune_level") val figFortuneLevel: Int,
    @FieldName("fig_personal_best") val figPersonalBest: Boolean,
    @FieldName("fig_personal_best_amount") val figPersonalBestAmount: Int,
    @FieldName("mangrove_fortune_level") val mangroveFortuneLevel: Int,
    @FieldName("mangrove_personal_best") val mangrovePersonalBest: Boolean,
    @FieldName("mangrove_personal_best_amount") val mangrovePersonalBestAmount: Int,
)
