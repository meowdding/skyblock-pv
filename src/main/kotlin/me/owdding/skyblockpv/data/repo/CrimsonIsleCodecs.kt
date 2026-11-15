package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.codecs.IncludedCodecs
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

@Module
object CrimsonIsleCodecs {
    val data: CrimsonIsleData by SkyBlockPv.repoDelegate("pv/crimson_isle", CrimsonIsleData())

    val factionRanks: List<ThresholdData<String>>
        get() = data.factionRanks

    val factionNameMap: Map<String, Component>
        get() = data.nameMap

    val kuudra: KuudraData
        get() = data.kuudra

    val dojo: DojoData
        get() = data.dojo

    @IncludedCodec(named = "ci§dojo§grades")
    internal val DOJO_GRADES: Codec<List<ThresholdData<Component>>> = ThresholdData.createCodec(IncludedCodecs.COMPONENT_TAG, "grade").listOf()

    @IncludedCodec(named = "ci§dojo§item")
    internal val DOJO_ITEM: Codec<List<ThresholdData<Lazy<ItemStack>>>> = ThresholdData.createCodec(IncludedCodecs.ITEM_REFERENCE, "item").listOf()

    @IncludedCodec(named = "ci§rep")
    internal val REP: Codec<List<ThresholdData<String>>> = ThresholdData.createCodec(Codec.STRING, "name").listOf()

    @IncludedCodec(named = "ci§name")
    internal val NAME: Codec<Map<String, Component>> = Codec.unboundedMap(Codec.STRING, IncludedCodecs.COMPONENT_TAG)

    @GenerateCodec
    data class CrimsonIsleData(
        @NamedCodec("ci§rep") @FieldName("faction_reputation") val factionRanks: List<ThresholdData<String>> = emptyList(),
        @NamedCodec("ci§name") @FieldName("faction_name_map") val nameMap: Map<String, Component> = emptyMap(),
        val dojo: DojoData = DojoData(),
        val kuudra: KuudraData = KuudraData(),
    )

    @GenerateCodec
    data class KuudraData(
        val collection: List<Int> = emptyList(),
        @FieldName("name_map") val idNameMap: Map<String, String> = emptyMap(),
        val requirements: Map<String, Int> = emptyMap(),
    ) {
        val ids: Set<String> get() = idNameMap.keys
    }

    @GenerateCodec
    data class DojoData(
        @NamedCodec("ci§dojo§grades") val grades: List<ThresholdData<Component>> = emptyList(),
        @NamedCodec("ci§dojo§item") val belts: List<ThresholdData<Lazy<ItemStack>>> = emptyList(),
        @FieldName("name_map") val idNameMap: Map<String, String> = emptyMap(),
    ) {
        val ids: Set<String> get() = idNameMap.keys
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
