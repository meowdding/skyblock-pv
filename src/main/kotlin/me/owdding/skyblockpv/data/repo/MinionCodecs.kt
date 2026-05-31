package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

@LoadData
object MinionCodecs : DefaultedData {
    private val defaultMiscData = MiscData(11, emptyMap())
    private var _miscData: MiscData? = null
    val miscData: MiscData get() = _miscData ?: defaultMiscData
    val categories: MutableList<MinionCategory> = mutableListOf()

    override suspend fun load() {
        Utils.loadRemoteRepoData<Data>("pv/minions").let { data ->
            this._miscData = data.miscData
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
