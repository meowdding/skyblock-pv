package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.codecs.CodecUtils

object CrimsonIsleCodecs {
    val factionRanks = mutableListOf<ThresholdData<String>>()
    val factionNameMap = mutableMapOf<String, Component>()

    object KuudraCodecs {
        val collection = mutableListOf<Int>()
        val idNameMap = mutableMapOf<String, String>()
        val requirements = mutableMapOf<String, Int>()
        val ids: Set<String> get() = idNameMap.keys

        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.INT.listOf().fieldOf("collection").forGetter { collection },
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("name_map").forGetter { idNameMap },
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("requirements").forGetter { requirements },
            ).apply(it) { collection, idNameMap, requirements ->
                this.collection.addAll(collection)
                this.idNameMap.putAll(idNameMap)
                this.requirements.putAll(requirements)
            }
        }
    }

    object DojoCodecs {
        val grades = mutableListOf<ThresholdData<Component>>()
        val belts = mutableListOf<ThresholdData<Lazy<ItemStack>>>()
        val idNameMap = mutableMapOf<String, String>()
        val ids: Set<String> get() = KuudraCodecs.idNameMap.keys

        val CODEC = RecordCodecBuilder.create {
            it.group(
                ThresholdData.createCodec(CodecUtils.COMPONENT_TAG, "grade").listOf().fieldOf("grades").forGetter { grades },
                ThresholdData.createCodec(CodecUtils.ITEM_REFRENCE, "item").listOf().fieldOf("belts").forGetter { belts },
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("name_map").forGetter { idNameMap },
            ).apply(it) { grades, belts, idNameMap ->
                this.grades.addAll(grades)
                this.belts.addAll(belts)
                this.idNameMap.putAll(idNameMap)
            }
        }
    }

    val CODEC = RecordCodecBuilder.create {
        it.group(
            ThresholdData.createCodec(Codec.STRING, "name").listOf().fieldOf("faction_reputation").forGetter { factionRanks },
            Codec.unboundedMap(Codec.STRING, CodecUtils.COMPONENT_TAG).fieldOf("faction_name_map").forGetter { factionNameMap },
            DojoCodecs.CODEC.fieldOf("dojo").forGetter {},
            KuudraCodecs.CODEC.fieldOf("kuudra").forGetter {},
        ).apply(it) { factionRanks, factionNameMap, _, _ ->
            this.factionRanks.addAll(factionRanks)
            this.factionNameMap.putAll(factionNameMap)
        }
    }

    init {
        Utils.loadFromRepo<JsonObject>("crimson_isle").toDataOrThrow(CODEC)
    }

    data class ThresholdData<T>(val threshold: Int, val data: T) {
        companion object {
            fun <T> createCodec(codec: Codec<T>, name: String): Codec<ThresholdData<T>> {
                return RecordCodecBuilder.create {
                    it.group(
                        Codec.INT.fieldOf("threshold").forGetter(ThresholdData<T>::threshold),
                        codec.fieldOf(name).forGetter(ThresholdData<T>::data),
                    ).apply(it, ::ThresholdData)
                }
            }
        }
    }
}
