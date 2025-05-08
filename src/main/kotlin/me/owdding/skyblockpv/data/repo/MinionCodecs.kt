package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

@LoadData
object MinionCodecs : ExtraData {
    lateinit var miscData: MiscData
    val categories: MutableList<MinionCategory> = mutableListOf()
    override fun load() {
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
