package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

@Module
object MinionCodecs {
    val miscData: MiscData
    val categories: MutableList<MinionCategory> = mutableListOf()

    init {
        Utils.loadRepoData<Data>("minions").let { data ->
            this.miscData = data.miscData
            this.categories.addAll(data.categories)
        }
    }

    @GenerateCodec
    @NamedCodec("MinionData")
    data class Data(
        @FieldName("data") val miscData: MiscData,
        val categories: List<MinionCategory>,
    )

    @GenerateCodec
    data class MiscData(
        @FieldName("default_max") val defaultMax: Int,
        @FieldName("max_tier_overrides") val maxOverwrites: Map<String, Int>,
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
