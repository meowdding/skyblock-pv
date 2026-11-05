package me.owdding.skyblockpv.feature.visitors

import me.owdding.lib.extensions.shorten
import me.owdding.skyblockpv.data.api.skills.farming.ActiveCommission
import me.owdding.skyblockpv.data.api.skills.farming.CommissionRequirement
import me.owdding.skyblockpv.data.api.skills.farming.GardenProfile
import me.owdding.skyblockpv.data.repo.StaticGardenData
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object VisitorRewardCalculator {

    fun calculateRewards(garden: GardenProfile, commission: ActiveCommission): Map<RewardType, Reward> {
        val rarity = commission.visitor?.rarity
        val requirements = commission.requirement

        return buildMap {

            if (rarity == null) {
                val min = calculateRewards(requirements, SkyBlockRarity.COMMON)
                val max = calculateRewards(requirements, SkyBlockRarity.SPECIAL)

                for (reward in min.keys + max.keys) {
                    put(reward, Reward.union(min[reward], max[reward]))
                }
                put(
                    RewardType.GARDEN_XP,
                    Reward.of(
                        baseGardenXp(garden) * VisitorRarityMultiplier.GARDEN_XP(SkyBlockRarity.COMMON),
                        baseGardenXp(garden) * VisitorRarityMultiplier.GARDEN_XP(SkyBlockRarity.SPECIAL),
                    ),
                )
            } else {
                putAll(calculateRewards(requirements, rarity))
                put(
                    RewardType.GARDEN_XP,
                    Reward.of(baseGardenXp(garden) * VisitorRarityMultiplier.GARDEN_XP(rarity)),
                )
            }
        }
    }

    fun calculateRewards(requirements: List<CommissionRequirement>, rarity: SkyBlockRarity) = requirements.map {
        calculateRewards(it, rarity)
    }.fold(mutableMapOf<RewardType, Reward>()) { acc, map ->
        map.forEach { (type, reward) ->
            acc[type] = reward + acc[type]
        }
        acc
    }

    fun calculateQuantity(id: SkyBlockId, amount: Int) = StaticGardenData.resolveBaseMultiplier(id) * amount

    fun calculateCopper(baseItems: Double, rarity: SkyBlockRarity, item: SkyBlockId) =
        (1.3 * baseItems * VisitorRarityMultiplier.COPPER(rarity)) / (2000 * StaticGardenData.quantityMultiplier(item))

    fun calculateBaseFarmingXp(baseItems: Double, item: SkyBlockId, rarity: SkyBlockRarity) =
        baseItems * StaticGardenData.baseFarmingXp(item) * VisitorRarityMultiplier.FARMING_XP(rarity)

    fun baseGardenXp(profile: GardenProfile) = when (val level = profile.getGardenLevel()) {
        1 -> 2
        2 -> 3
        7 -> 8
        8 -> 10
        9 -> 12
        in 10..15 -> 15
        else -> level
    }

    fun calculateRewards(requirement: CommissionRequirement, rarity: SkyBlockRarity): Map<RewardType, Reward> {
        val minRawQuantity = calculateQuantity(requirement.originalItem, requirement.originalAmount - 1)
        val maxRawQuantity = ceil(calculateQuantity(requirement.originalItem, requirement.originalAmount) * 1.1)
        val baseItem = StaticGardenData.resolveBase(requirement.originalItem)

        val quantityQuantor = (StaticGardenData.resolveBaseMultiplier(baseItem) * VisitorRarityMultiplier.QUANTITY(rarity))
        val minBaseItems = minRawQuantity / quantityQuantor
        val maxBaseItems = maxRawQuantity / quantityQuantor

        return buildMap {
            put(
                RewardType.COPPER,
                Reward.of(
                    max(2.0, calculateCopper(minBaseItems, rarity, baseItem)),
                    max(2.0, calculateCopper(maxBaseItems, rarity, baseItem)),
                ),
            )
            put(
                RewardType.FARMING_XP,
                Reward.of(
                    calculateBaseFarmingXp(minBaseItems, baseItem, rarity),
                    calculateBaseFarmingXp(maxBaseItems, baseItem, rarity),
                ),
            )
        }
    }

    enum class RewardType(val displayName: Component) {
        FARMING_XP(Text.of("Farming XP", TextColor.GRAY)) {
            override fun format(amount: Int): MutableComponent = Text.of(amount.shorten(), TextColor.DARK_AQUA)
        },
        GARDEN_XP(Text.of("Garden Experience", TextColor.GRAY)) {
            override fun format(amount: Int): MutableComponent = Text.of(amount.toFormattedString(), TextColor.DARK_GREEN)
        },
        COPPER(Text.of("Copper", TextColor.RED)) {
            override fun format(amount: Int) = Text.of(amount.toFormattedString(), TextColor.RED)
        },
        ;

        abstract fun format(amount: Int): MutableComponent
    }

    enum class VisitorRarityMultiplier(val uncommon: Double, val rare: Double, val legendary: Double) {
        QUANTITY(1.0, 1.5, 3.0),
        FARMING_XP(0.05, 0.06, 0.75),
        GARDEN_XP(1.0, 2.0, 5.0),
        COPPER(1.0, 1.0, 5.0),
        ;

        operator fun invoke(rarity: SkyBlockRarity) = when {
            rarity < SkyBlockRarity.RARE -> uncommon
            rarity < SkyBlockRarity.LEGENDARY -> rare
            else -> legendary
        }
    }

    sealed interface Reward {
        companion object {
            fun of(single: Int): Reward = SingleReward(single)
            fun of(single: Double): Reward = of(single, single)

            fun of(min: Double, max: Double): Reward {
                val min = max(0, floor(min).toInt())
                val max = max(0, ceil(max).toInt())

                return if (max == min) {
                    SingleReward(min)
                } else {
                    RewardRange(min, max)
                }
            }

            fun union(reward: Reward?, reward2: Reward?): Reward {
                require(reward != null || reward2 != null) { "At least one argument must not be null!" }
                return when {
                    reward != null && reward2 != null -> reward.union(reward2)
                    reward2 != null -> reward2
                    reward != null -> reward
                    else -> throw RuntimeException("At least one argument must not be null!")
                }
            }
        }

        data class RewardRange(override val min: Int, override val max: Int) : Reward {
            override fun plus(other: Reward?): Reward = if (other == null) this else RewardRange(this.min + other.min, this.max + other.max)
        }

        data class SingleReward(val amount: Int) : Reward {
            override val min: Int = amount
            override val max: Int = amount
            override fun plus(other: Reward?): Reward = when (other) {
                null -> this
                is SingleReward -> SingleReward(this.amount + other.amount)
                is RewardRange -> RewardRange(this.amount + other.min, this.amount + other.max)
            }

        }

        fun union(other: Reward) = of(
            min(this.min, other.min).toDouble(),
            max(this.max, other.max).toDouble(),
        )

        val min: Int
        val max: Int

        operator fun plus(other: Reward?): Reward
    }

}
