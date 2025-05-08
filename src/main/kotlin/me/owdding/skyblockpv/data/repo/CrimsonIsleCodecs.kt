package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

@LoadData
object CrimsonIsleCodecs : ExtraData {
    val factionRanks = mutableListOf<ThresholdData<String>>()
    val factionNameMap = mutableMapOf<String, Component>()

    object KuudraCodecs {
        val collection = mutableListOf<Int>()
        val idNameMap = mutableMapOf<String, String>()
        val requirements = mutableMapOf<String, Int>()
        val ids: Set<String> get() = idNameMap.keys

        @NamedCodec("KuudraData")
        @GenerateCodec
        data class Data(
            val collection: List<Int>,
            @FieldName("name_map") val idNameMap: Map<String, String>,
            val requirements: Map<String, Int>,
        )

        fun load(data: Data) {
            this.collection.addAll(data.collection)
            this.idNameMap.putAll(data.idNameMap)
            this.requirements.putAll(data.requirements)
        }
    }

    object DojoCodecs {
        val grades = mutableListOf<ThresholdData<Component>>()
        val belts = mutableListOf<ThresholdData<Lazy<ItemStack>>>()
        val idNameMap = mutableMapOf<String, String>()
        val ids: Set<String> get() = idNameMap.keys

        @IncludedCodec(named = "ci§dojo§grades")
        val GRADES: Codec<List<ThresholdData<Component>>> = ThresholdData.createCodec(CodecUtils.COMPONENT_TAG, "grade").listOf()

        @IncludedCodec(named = "ci§dojo§item")
        val ITEM: Codec<List<ThresholdData<Lazy<ItemStack>>>> = ThresholdData.createCodec(CodecUtils.ITEM_REFERENCE, "item").listOf()

        @NamedCodec("DojoData")
        @GenerateCodec
        data class Data(
            @NamedCodec("ci§dojo§grades") val grades: List<ThresholdData<Component>>,
            @NamedCodec("ci§dojo§item") val belts: List<ThresholdData<Lazy<ItemStack>>>,
            @FieldName("name_map") val idNameMap: Map<String, String>,
        )

        fun load(dojo: Data) {
            this.grades.addAll(dojo.grades)
            this.belts.addAll(dojo.belts)
            this.idNameMap.putAll(dojo.idNameMap)
        }
    }


    @IncludedCodec(named = "ci§rep")
    val REP: Codec<List<ThresholdData<String>>> = ThresholdData.createCodec(Codec.STRING, "name").listOf()

    @IncludedCodec(named = "ci§name")
    val NAME: Codec<Map<String, Component>> = Codec.unboundedMap(Codec.STRING, CodecUtils.COMPONENT_TAG)

    @GenerateCodec
    @NamedCodec("CiData")
    data class Data(
        @NamedCodec("ci§rep") @FieldName("faction_reputation") val factionRanks: List<ThresholdData<String>>,
        @NamedCodec("ci§name") @FieldName("faction_name_map") val nameMap: Map<String, Component>,
        val dojo: DojoCodecs.Data,
        val kuudra: KuudraCodecs.Data,
    )

    override fun load() {
        val data = Utils.loadRepoData<Data>("crimson_isle")
        this.factionNameMap.putAll(data.nameMap)
        this.factionRanks.addAll(data.factionRanks)
        DojoCodecs.load(data.dojo)
        KuudraCodecs.load(data.kuudra)
    }

    fun <T> List<ThresholdData<T>>.getFor(amount: Int): T? {
        return this.sortedByDescending { (threshold, _) -> threshold }.firstOrNull { (threshold, _) -> threshold <= amount }?.data
            ?: this.firstOrNull { (threshold, _) -> threshold == 0 }?.data
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
