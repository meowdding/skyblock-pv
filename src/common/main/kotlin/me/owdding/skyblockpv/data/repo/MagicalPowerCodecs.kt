package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.generated.DispatchHelper
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import kotlin.math.roundToInt
import kotlin.reflect.KClass

@LoadData
object MagicalPowerCodecs : ExtraData {
    lateinit var data: MagicalPowerRepoData
        private set

    private val anniversaryIds = setOf(
        "party_hat_crab",
        "party_hat_sloth",
        "balloon_hat",
    )

    override suspend fun load() {
        data = Utils.loadRepoData<MagicalPowerRepoData>("magical_power")
    }

    fun calculateMagicalPower(profile: SkyBlockProfile): Int {
        val items = (profile.inventory?.talismans ?: emptyList()).flatMap { it.talismans.inventory }

        val base = items.associateWith { next ->
            val override = data.overrides[next.getSkyBlockId()]
            val mp = data.rarity[next.getData(DataTypes.RARITY)] ?: 0

            override?.apply(mp) ?: mp
        }.filterDuplicates().toMutableMap()

        val riftPrism = if (profile.maxwell?.consumedRiftPrism == true) {
            base.filter { it.key[DataTypes.SKYBLOCK_ID]?.cleanId.equals("rift_prism", true) }

            11
        } else 0
        val hasAbicase = items.any { it[DataTypes.SKYBLOCK_ID]?.cleanId?.startsWith("abicase_", true) == true }
        val abicase = if (hasAbicase) profile.maxwell?.abiphoneContacts?.floorDiv(2) ?: 0 else 0

        return base.values.sum() + riftPrism + abicase
    }

    private fun Map<ItemStack, Int>.filterDuplicates(): Map<ItemStack, Int> {
        val pairs = map { it.key to it.value }
            .sortedByDescending { it.second }
            .distinctBy { it.first.getSkyBlockId() }
        val ids = pairs.mapNotNull { it.first.getSkyBlockId() }

        val filtered = ids.filter { NeuMiscCodecs.data.hasHigherTier(it, ids) }.toMutableSet()

        val hatItems = pairs.filter { it.first.getSkyBlockId()?.cleanId?.let { id -> anniversaryIds.any { hat -> id.startsWith(hat, true) } } == true }

        if (hatItems.size > 1) {
            val bestHat = hatItems.maxByOrNull { it.second }
            hatItems.filter { it != bestHat }.forEach { filtered.add(it.first.getSkyBlockId()!!) }
        }

        return pairs.filter { it.first.getSkyBlockId() !in filtered }.toMap()
    }

    @GenerateCodec
    data class MagicalPowerRepoData(
        val rarity: Map<SkyBlockRarity, Int>,
        val overrides: Map<SkyBlockId, Override>,
    )

    abstract class Override(val type: OverrideTypes) {
        abstract fun apply(num: Int): Int
    }

    @GenerateCodec
    data class MultiplyOverride(val value: Double) : Override(OverrideTypes.MULTIPLY) {
        override fun apply(num: Int) = (num * value).roundToInt()
    }

    @GenerateDispatchCodec(Override::class)
    enum class OverrideTypes(override val type: KClass<out Override>) : DispatchHelper<Override> {
        MULTIPLY(MultiplyOverride::class),
        ;

        companion object {
            fun getType(id: String) = valueOf(id.uppercase())
        }
    }
}
