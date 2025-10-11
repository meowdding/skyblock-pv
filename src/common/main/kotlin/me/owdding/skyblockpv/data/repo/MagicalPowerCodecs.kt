package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.generated.DispatchHelper
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.Utils.removeIf
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
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

    private val RIFT_PRISM = SkyBlockId.item("rift_prism")

    override suspend fun load() {
        data = Utils.loadRepoData<MagicalPowerRepoData>("magical_power")
    }

    fun calculateMagicalPower(profile: SkyBlockProfile): Pair<Int, Component> {
        val items = (profile.inventory?.talismans ?: emptyList()).flatMap { it.talismans.inventory }

        val base = items.associateWith { data.getMagicalPower(it) }.filterDuplicates().toMutableMap()

        val riftPrism = if (profile.maxwell?.consumedRiftPrism == true) {
            base.removeIf { (k, _) -> k.getSkyBlockId() == RIFT_PRISM }
            11
        } else 0
        val hasAbicase = items.any { it.getSkyBlockId()?.cleanId?.startsWith("abicase_", true) == true }
        val abicase = if (hasAbicase) profile.maxwell?.abiphoneContacts?.floorDiv(2) ?: 0 else 0

        val lore = TooltipBuilder.multiline {
            add("Magical Power Breakdown:") { color = TextColor.GRAY }
            base.toList()
                .sortedByDescending { it.first.getData(DataTypes.RARITY) }
                .groupBy { it.first.getData(DataTypes.RARITY) }
                .forEach { (rarity, items) ->
                    val rarity = rarity ?: return@forEach
                    val totalPower = items.sumOf { it.second }
                    add("") {
                        color = TextColor.ORANGE

                        append(rarity.displayText)
                        append(": +${totalPower.toFormattedString()}")
                        append(" (${items.size})") { color = TextColor.GRAY }
                    }
                }

            space()
            add("Rift Prism:") {
                color = TextColor.BLUE
                append(" +${11.takeIf { profile.maxwell?.consumedRiftPrism == true } ?: 0}") { color = TextColor.GOLD }
            }
            add("Abiphone Case:") {
                color = TextColor.BLUE
                append(" +${abicase}") { color = TextColor.GOLD }
            }

        }

        return base.values.sum() + riftPrism + abicase to lore
    }

    private fun Map<ItemStack, Int>.filterDuplicates(): Map<ItemStack, Int> {
        val pairs = toList().sortedByDescending { it.second }.distinctBy { it.first.getSkyBlockId() }
        val ids = pairs.mapNotNull { it.first.getSkyBlockId() }

        val talismanToRemove = ids.filter { NeuMiscCodecs.data.hasHigherTier(it, ids) }.toMutableSet()

        val hatItems = pairs.filter { it.first.getSkyBlockId()?.cleanId?.let { id -> anniversaryIds.any { hat -> id.startsWith(hat, true) } } == true }
        if (hatItems.size > 1) {
            val bestHat = hatItems.maxByOrNull { it.second }
            hatItems.filterNot { it == bestHat }.forEach { talismanToRemove.add(it.first.getSkyBlockId()!!) }
            talismanToRemove.remove(bestHat?.first?.getSkyBlockId())
        }

        return pairs.filterNot { it.first.getSkyBlockId() in talismanToRemove }.toMap()
    }

    @GenerateCodec
    data class MagicalPowerRepoData(
        val rarity: Map<SkyBlockRarity, Int>,
        val overrides: Map<SkyBlockId, Override>,
    ) {
        fun getMagicalPower(item: ItemStack): Int {
            val override = overrides[item.getSkyBlockId()]
            val mp = rarity[item.getData(DataTypes.RARITY)] ?: 0

            return override?.apply(mp) ?: mp
        }
    }

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
