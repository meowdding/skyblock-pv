package me.owdding.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow

@Module
object MinionCodecs {
    lateinit var miscData: MiscData
    val categories: MutableList<MinionCategory> = mutableListOf()

    private val CATEGORY_CODEC: Codec<MinionCategory> = RecordCodecBuilder.create {
        it.group(
            Codec.INT.fieldOf("index").forGetter(MinionCategory::index),
            CodecUtils.ITEM_REFERENCE.fieldOf("display").forGetter(MinionCategory::display),
            Codec.STRING.listOf().fieldOf("minions").forGetter(MinionCategory::minions),
            CodecUtils.COMPONENT_TAG.fieldOf("title").forGetter(MinionCategory::title),
        ).apply(it, ::MinionCategory)
    }

    private val MISC_CODEC: Codec<MiscData> = RecordCodecBuilder.create {
        it.group(
            Codec.INT.fieldOf("default_max").forGetter(MiscData::defaultMax),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("max_tier_overrides").forGetter(MiscData::maxOverwrites),
        ).apply(it, ::MiscData)
    }

    val CODEC: Codec<Unit> = RecordCodecBuilder.create {
        it.group(
            MISC_CODEC.fieldOf("data").forGetter { miscData },
            CATEGORY_CODEC.listOf().fieldOf("categories").forGetter { categories },
        ).apply(it) { miscData, categories ->
            this.miscData = miscData
            this.categories.addAll(categories)
            Unit
        }
    }

    init {
        Utils.loadFromRepo<JsonObject>("minions").toDataOrThrow(CODEC)
    }

    data class MiscData(val defaultMax: Int, val maxOverwrites: Map<String, Int>) {
        fun getMax(type: String) = maxOverwrites.getOrDefault(type, defaultMax)
    }

    data class MinionCategory(val index: Int, val display: Lazy<ItemStack>, val minions: List<String>, val title: Component)

}
