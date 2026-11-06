package me.owdding.skyblockpv.data.api.skills.farming

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.ktcodecs.FieldName
import me.owdding.lib.utils.FeatureName
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.data.api.skills.PowderType
import me.owdding.skyblockpv.data.repo.*
import me.owdding.skyblockpv.utils.json.getAs
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.data.Essence as EssenceEnum
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.repo.LazyItemStack
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

data class GardenData(
    val copper: Int,
    val larvaConsumed: Int,
    val glowingMushroomBroken: Int,
    val analyzedCrops: Set<String>,
    val discoveredCrops: Set<String>,
) {
    companion object {
        val EMPTY = GardenData(0, 0, 0, emptySet(), emptySet())
    }
}

data class GardenProfile(
    val unlockedPlots: List<StaticPlotData>,
    val selectedBarnSkin: ItemStack,
    val commissionData: CommissionData,
    val activeCommission: Map<String, ActiveCommission>,
    val gardenExperience: Long,
    val unlockedBarnSkins: List<ItemStack>,
    val composterData: ComposterData,
    val resourcesCollected: Map<GardenResource, Long>,
    val cropUpgradeLevels: Map<GardenResource, Short>,
    val greenhouseSlots: List<Vector2i>,
    val gardenUpgrades: Map<GardenUpgrades, Int>,
) {
    @FeatureName("GardenProfileParser")
    companion object : MeowddingLogger by SkyBlockPv.featureLogger() {
        private fun <T> JsonObject?.asGardenResourceMap(mapper: (JsonElement?) -> T): Map<GardenResource, T> {
            if (this == null) return emptyMap()

            return this.asMap { s, jsonElement -> GardenResource.getByApiId(s) to mapper(jsonElement) }
        }

        fun fromJson(result: JsonObject): GardenProfile {
            return GardenProfile(
                unlockedPlots = result.getAs<JsonArray>("unlocked_plots_ids")?.mapNotNull {
                    StaticGardenData.plots.find { plot -> it.asString == plot.id }
                } ?: emptyList(),
                selectedBarnSkin = result.get("selected_barn_skin").toBarnSkin().create(),
                commissionData = result.getAs<JsonObject>("commission_data").toCommissionData(),
                activeCommission = result.getAs<JsonObject>("active_commissions").toActiveCommissionData(),
                gardenExperience = result.get("garden_experience").asLong(0),
                unlockedBarnSkins = result.getAs<JsonArray>("unlocked_barn_skins")?.map { it.toBarnSkin().create() } ?: emptyList(),
                composterData = result.getAs<JsonObject>("composter_data").toComposterData(),
                resourcesCollected = result.getAs<JsonObject>("resources_collected").asGardenResourceMap { it.asLong(0) },
                cropUpgradeLevels = result.getAs<JsonObject>("crop_upgrade_levels").asGardenResourceMap { it.asShort(0) },
                greenhouseSlots = result.getAs<JsonArray>("greenhouse_slots")?.filterIsInstance<JsonObject>()?.map {
                    Vector2i(it.get("x").asInt(0), it.get("z").asInt(0))
                } ?: emptyList(),
                gardenUpgrades = result.getAs<JsonObject>("garden_upgrades").asMap { string, element ->
                    runCatching { GardenUpgrades.valueOf(string) }.getOrNull() to element.asInt(0)
                }.filterKeysNotNull(),
            )
        }

        private fun JsonObject?.toComposterData(): ComposterData {
            val upgrades = this?.getAs<JsonObject>("upgrades")
                .asMap { key, value -> ComposterUpgrade.valueOf(key.uppercase()) to value.asInt(0) }.toMutableMap()

            ComposterUpgrade.entries.forEach {
                upgrades.putIfAbsent(it, 0)
            }

            return ComposterData(
                organicMatter = this?.get("organic_matter").asDouble(0.0),
                fuel = this?.get("fuel_units").asDouble(0.0),
                compostUnits = this?.get("compost_units").asDouble(0.0),
                compostItems = this?.get("compost_items").asInt(0),
                conversionTicks = this?.get("conversion_ticks").asInt(0),
                lastUpdateTimestamp = this?.get("last_save").asLong(0),
                upgrades = upgrades,
            )
        }

        private fun JsonElement?.toBarnSkin(): LazyItemStack {
            val string = this?.asString ?: return DefaultBarnSkin.unknown("null").getItem()
            return StaticGardenData.barnSkins[string]?.getItem()
                ?: SkyBlockItemsRepo.getLazyItemStack("${string}_BARN_SKIN")
                ?: DefaultBarnSkin.unknown(string).getItem()
        }

        private fun JsonObject?.toCommissionData(): CommissionData {
            val visits = this?.getAs<JsonObject>("visits").asMap { id, value -> id to value.asInt }
            val completed = this?.getAs<JsonObject>("completed")
            val commissions = visits.mapNotNull { (id, visit) ->
                id.let { StaticGardenData.visitors.find { it.id == id } }
                    .let {
                        if (it == null) {
                            warn("Unknown visitor with id '$id'")
                        }
                        Commission(it, visit, completed?.get(id).asInt(0), id)
                    }
            }


            return CommissionData(
                commissions = commissions,
                totalCompleted = this?.get("total_completed").asInt(0),
                uniqueVisitorsServed = this?.get("unique_npcs_served").asInt(0),
            )
        }

        private fun JsonObject?.toActiveCommissionData(): Map<String, ActiveCommission> {
            val json = this ?: return emptyMap()
            return json.entrySet().associate { (id, el) ->
                val obj = el.asJsonObject
                val visitor = StaticGardenData.visitors.find { it.id == id }

                val requirements = obj.getAsJsonArray("requirement")?.map { el ->
                    val obj = el.asJsonObject
                    CommissionRequirement(
                        SkyBlockId.item(obj.get("original_item").asString),
                        obj.get("original_amount").asInt,
                        SkyBlockId.item( obj.get("item").asString),
                        obj.get("amount").asInt,
                    )
                } ?: emptyList()

                val bonusRewards = obj.getAsJsonArray("bonus_rewards")?.map {
                    CommissionBonusReward(SkyBlockId.item(it.asJsonObject.get("item_id").asString))
                } ?: emptyList()

                val extraRewards = obj.getAsJsonArray("extra_rewards")?.mapNotNull { rewardEl ->
                    val rewardObj = rewardEl.asJsonObject
                    when {
                        rewardObj.has("powder") -> {
                            val powderStr = rewardObj.get("powder").asString.uppercase()
                            val powderType = valueOfOrNull<PowderType>(powderStr) ?: return@mapNotNull null
                            ExtraReward.Powder(powderType, rewardObj.get("amount").asInt(0))
                        }
                        rewardObj.has("enchant") -> {
                            ExtraReward.Enchant(SkyBlockId.enchantment(rewardObj.get("enchant").asString, rewardObj.get("level").asInt(1)))
                        }
                        rewardObj.has("rune") -> {
                            ExtraReward.Rune(SkyBlockId.rune(rewardObj.get("rune").asString, 1))
                        }
                        rewardObj.has("item") && rewardObj.has("red") -> {
                            ExtraReward.ColoredItem(
                                SkyBlockId.item(rewardObj.get("item").asString),
                                rewardObj.get("red").asInt(0),
                                rewardObj.get("green").asInt(0),
                                rewardObj.get("blue").asInt(0)
                            )
                        }
                        rewardObj.has("candy") -> {
                            ExtraReward.Candy(SkyBlockId.item(rewardObj.get("candy").asString))
                        }
                        rewardObj.has("sowdust") -> {
                            ExtraReward.Sowdust(rewardObj.get("sowdust").asInt(0))
                        }
                        rewardObj.has("essence") -> {
                            val essenceStr = rewardObj.get("essence").asString.uppercase()
                            val essenceType = valueOfOrNull<EssenceEnum>(essenceStr) ?: return@mapNotNull null
                            ExtraReward.Essence(rewardObj.get("amount").asInt(0), essenceType)
                        }
                        else -> null
                    }
                } ?: emptyList()

                id to ActiveCommission(
                    visitor,
                    requirements,
                    obj.get("status")?.asString?.let { valueOfOrNull<CommissionStatus>(it) } ?: CommissionStatus.NOT_STARTED,
                    obj.get("position").asInt(0),
                    bonusRewards,
                    extraRewards,
                )
            }
        }
    }

    fun getGardenLevel(): Int = StaticGardenData.miscData.getLevelForExperience(gardenExperience)
}

data class CommissionData(
    val commissions: List<Commission>,
    val totalCompleted: Int,
    val uniqueVisitorsServed: Int,
)

data class Commission(
    val visitor: StaticVisitorData?,
    val total: Int,
    val accepted: Int,
    val visitorId: String,
)

enum class ComposterUpgrade(val fakeUpgrade: Boolean = false) {
    SPEED,
    MULTI_DROP,
    FUEL_CAP,
    ORGANIC_MATTER_CAP,
    COST_REDUCTION,
    COMPOST_SPEED_ATTRIBUTE(fakeUpgrade = true)
}

data class ComposterData(
    val organicMatter: Double,
    val fuel: Double,
    val compostUnits: Double,
    val compostItems: Int,
    val conversionTicks: Int,
    val lastUpdateTimestamp: Long,
    val upgrades: Map<ComposterUpgrade, Int>,
)

data class ActiveCommission(
    val visitor: StaticVisitorData?,
    val requirement: List<CommissionRequirement>,
    val status: CommissionStatus,
    val position: Int,
    val bonusRewards: List<CommissionBonusReward>,
    val extraRewards: List<ExtraReward>,
)

data class CommissionRequirement(
    @FieldName("original_item") val originalItem: SkyBlockId,
    @FieldName("original_amount") val originalAmount: Int,
    val item: SkyBlockId,
    val amount: Int,
)

enum class CommissionStatus(val color: Int) {
    NOT_STARTED(TextColor.GREEN),
    STARTED(TextColor.RED),
}

data class CommissionBonusReward(val itemId: SkyBlockId)

sealed interface ExtraReward {
    fun asComponent(): Component

    interface ItemLikeReward : ExtraReward {
        operator fun component1(): SkyBlockId

        override fun asComponent(): Component = Text.of {
            val id = component1()
            append(id.toItem().hoverName)

            if (id.isEnchantment) {
                append(id.toItem().getLore().firstOrNull { it.stripped.isNotEmpty() } ?: return@of)
            }
        }
    }

    data class Powder(val powder: PowderType, val amount: Int) : ExtraReward {
        override fun asComponent(): Component = Text.of {
            append(amount.toFormattedString())
            append(" ")
            append(powder.name.toTitleCase())
            append(" Powder")

            withStyle(powder.formatting)
        }
    }

    data class Essence(val amount: Int, val essence: EssenceEnum) : ExtraReward {
        override fun asComponent(): Component = Text.of {
            append(amount.toFormattedString())
            append(" ")
            append(essence.name.toTitleCase())
            this.color = TextColor.PINK
        }
    }
    data class Enchant(val enchant: SkyBlockId) : ItemLikeReward
    data class Rune(val rune: SkyBlockId) : ItemLikeReward

    data class ColoredItem(val item: SkyBlockId, val red: Int, val green: Int, val blue: Int) : ExtraReward {
        override fun asComponent(): Component = Text.of {
            append(item.toItem().hoverName)
            val color = ARGB.color(red, green, blue)
            append(" (#${(color and 0xffffff).toString(16).lowercase()})", color)
        }
    }
    data class Candy(val candy: SkyBlockId) : ItemLikeReward

    data class Sowdust(val amount: Int) : ExtraReward {
        override fun asComponent() = Text.of {
            append(amount.toFormattedString())
            append(" Sowdust")
            this.color = TextColor.DARK_GREEN
        }
    }
}
