package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

@Module
object MinionCodecs {

    val data: MinionData by SkyBlockPv.repoDelegate("pv/minions", MinionData())
    val miscData: MiscData
        get() = data.miscData
    val categories: List<MinionCategory>
        get() = data.categories

    @GenerateCodec
    data class MinionData(
        @FieldName("data") val miscData: MiscData = MiscData(),
        val categories: List<MinionCategory> = emptyList(),
    )

    @GenerateCodec
    data class MiscData(
        @FieldName("default_max") val defaultMax: Int = 11,
        @FieldName("max_tier_overrides") val maxOverwrites: Map<String, Int> = emptyMap(),
    ) {
        fun getMax(type: String) = maxOverwrites.getOrDefault(type, defaultMax)
    }

    @GenerateCodec
    data class MinionCategory(
        val index: Int,
        @NamedCodec("lazy_item_ref") val display: Lazy<ItemStack>,
        val minions: List<String>,
        @NamedCodec("component_tag") val title: Component,
    )

}
