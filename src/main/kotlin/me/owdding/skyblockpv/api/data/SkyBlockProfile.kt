package me.owdding.skyblockpv.api.data

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.CollectionAPI
import me.owdding.skyblockpv.api.SkillAPI
import me.owdding.skyblockpv.data.SortedEntry.Companion.sortToCollectionsOrder
import me.owdding.skyblockpv.data.SortedEntry.Companion.sortToSkillsOrder
import me.owdding.skyblockpv.data.SortedEntry.Companion.sortToSlayerOrder
import me.owdding.skyblockpv.data.api.*
import me.owdding.skyblockpv.data.api.Currency
import me.owdding.skyblockpv.data.api.skills.*
import me.owdding.skyblockpv.data.api.skills.combat.*
import me.owdding.skyblockpv.data.api.skills.farming.FarmingData
import me.owdding.skyblockpv.data.api.skills.farming.GardenData
import me.owdding.skyblockpv.data.repo.EssenceData
import me.owdding.skyblockpv.feature.NetworthCalculator
import me.owdding.skyblockpv.utils.ChatUtils
import me.owdding.skyblockpv.utils.Utils.toDashlessString
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.json.getPathAs
import net.minecraft.Util
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.asStringList
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.util.*

data class SkyBlockProfile(
    val selected: Boolean,
    val id: ProfileId,
    val userId: UUID,
    val profileType: ProfileType = ProfileType.UNKNOWN,

    val currency: Currency?,
    val bank: Bank?,
    val inventory: InventoryData?,
    /**Level to Progress*/
    val skyBlockLevel: Pair<Int, Int>,
    val firstJoin: Long,
    val fairySouls: Int,
    val skill: Map<String, Long>,
    val collections: List<CollectionItem>?,
    val mobData: List<MobData>,
    val bestiaryData: List<BestiaryMobData>,
    val slayer: Map<String, SlayerTypeData>,
    val dungeonData: DungeonData?,
    val mining: MiningCore?,
    val forge: Forge?,
    val glacite: GlaciteData?,
    val tamingLevelPetsDonated: List<String>,
    val pets: List<Pet>,
    val petMilestones: Map<String, Int>,
    val trophyFish: TrophyFishData,
    val miscFishData: FishData,
    val essenceUpgrades: Map<String, Int>,
    val gardenData: GardenData,
    val farmingData: FarmingData,
    val chocolateFactoryData: CfData?,
    val rift: RiftData?,
    val crimsonIsleData: CrimsonIsleData,
    val minions: List<String>?,
    val maxwell: Maxwell?,
) {
    val netWorth by lazy { NetworthCalculator.calculateNetworthAsync(this) }

    companion object {

        fun fromJson(json: JsonObject, user: UUID): SkyBlockProfile? {
            val member = json.getPath("members.${user.toDashlessString()}")?.asJsonObject ?: return null
            val playerStats = member.getAs<JsonObject>("player_stats")
            val playerData = member.getAs<JsonObject>("player_data")
            val profile = member.getAs<JsonObject>("profile")

            return SkyBlockProfile(
                selected = json.getAs<Boolean>("selected") == true,
                id = ProfileId(
                    id = json.getAs("profile_id") ?: Util.NIL_UUID,
                    name = json.getAs("cute_name") ?: "Unknown",
                ),
                userId = user,
                profileType = when (json.getAs<String>("game_mode")) {
                    "ironman" -> ProfileType.IRONMAN
                    "island" -> ProfileType.STRANDED
                    "bingo" -> ProfileType.BINGO
                    else -> ProfileType.NORMAL
                },
                inventory = member.getAs<JsonObject>("inventory")?.let { InventoryData.fromJson(it, member.getAsJsonObject("shared_inventory")) },
                currency = member.getAs<JsonObject>("currencies")?.let { Currency.fromJson(it) },
                bank = Bank.fromJson(json, member),
                firstJoin = profile?.getAs<Long>("first_join") ?: 0,
                fairySouls = member.getPathAs<Int>("fairy_soul.total_collected") ?: 0,
                skyBlockLevel = run {
                    val experience = member.getPathAs<Int>("leveling.experience") ?: 0

                    experience / 100 to (experience % 100)
                },
                skill = playerData.getSkillData(),
                collections = member.getCollectionData(),
                mobData = playerStats?.getMobData() ?: emptyList(),
                bestiaryData = member.getPath("bestiary")?.asJsonObject?.getBestiaryMobData() ?: emptyList(),
                slayer = member.getAs<JsonObject>("slayer")?.getSlayerData() ?: emptyMap(),
                dungeonData = member.getAs<JsonObject>("dungeons")?.let { DungeonData.fromJson(it) },
                mining = member.getAs<JsonObject>("mining_core")?.let { MiningCore.fromJson(it) },
                forge = member.getAs<JsonObject>("forge")?.let { Forge.fromJson(it) },
                glacite = member.getAs<JsonObject>("glacite_player_data")?.let { GlaciteData.fromJson(it) },
                tamingLevelPetsDonated = member.getPath("pets_data.pet_care.pet_types_sacrificed").asStringList().filter { it.isNotBlank() },
                pets = member.getPathAs<JsonArray>("pets_data.pets")?.map { Pet.fromJson(it.asJsonObject) } ?: emptyList(),
                trophyFish = TrophyFishData.fromJson(member),
                miscFishData = FishData.fromJson(member, playerStats, playerData),
                essenceUpgrades = playerData?.getAs<JsonObject>("perks").parseEssencePerks(),
                petMilestones = playerStats?.getPath("pets.milestone").asMap { id, amount -> id to amount.asInt(0) },
                gardenData = run {
                    val data = member.getAs<JsonObject>("garden_player_data") ?: return@run GardenData(0, 0, 0)

                    return@run GardenData(
                        data.getAs<Int>("copper") ?: 0,
                        data.getAs<Int>("larva_consumed") ?: 0,
                        playerStats?.getAs<Int>("glowing_mushrooms_broken") ?: 0,
                    )
                },
                farmingData = FarmingData.fromJson(member.getAs("jacobs_contest")),
                chocolateFactoryData = member.getPath("events.easter")?.let { CfData.fromJson(it.asJsonObject) },
                rift = playerStats?.getAs<JsonObject>("rift")?.let { stats -> RiftData.fromJson(member.getAs("rift"), stats) },
                crimsonIsleData = CrimsonIsleData.fromJson(member.getAs("nether_island_player_data")),
                minions = playerData?.getAs<JsonArray>("crafted_generators")?.asStringList()
                    ?.filter { it.isNotBlank() }
                    ?.sortedByDescending { it.filter { it.isDigit() }.toIntOrNull() ?: -1 },
                maxwell = member.getAs<JsonObject>("accessory_bag_storage")?.let { Maxwell.fromJson(it) },
            )
        }

        private fun JsonObject?.getSkillData(): Map<String, Long> {
            val skills = this?.getAs<JsonElement>("experience").asMap { id, amount -> id to amount.asLong(0) }
                .filterKeys { it != "SKILL_DUNGEONEERING" }
                .toMutableMap()

            SkillAPI.Skills.entries.forEach { skill ->
                skills.putIfAbsent(skill.skillApiId, 0)
            }

            return skills.sortToSkillsOrder()
        }

        private fun JsonObject.getCollectionData(): List<CollectionItem>? {
            val collections = this["collection"] ?: return null
            val playerCollections = collections.asMap { id, amount -> id to amount.asLong(0) }
            val allCollections =
                CollectionAPI.collectionData.entries.flatMap { it.value.items.entries }.associate { it.key to it.value }.sortToCollectionsOrder()
            return allCollections.map { (id, _) ->
                id to (playerCollections[id] ?: 0)
            }.mapNotNull { (id, amount) ->
                CollectionAPI.getCategoryByItemName(id)?.let { CollectionItem(it, id, amount) }
            }
        }

        private fun JsonObject.getMobData(): List<MobData> {
            val deaths = this["deaths"].asMap { id, amount -> id to amount.asLong(0) }
            val kills = this["kills"].asMap { id, amount -> id to amount.asLong(0) }

            return (deaths.keys + kills.keys).toSet().map { id ->
                MobData(
                    mobId = id,
                    kills = kills[id] ?: 0,
                    deaths = deaths[id] ?: 0,
                )
            }
        }

        private fun JsonObject.getBestiaryMobData(): List<BestiaryMobData> {
            val deaths = this["deaths"].asMap { id, amount -> id to amount.asLong(0) }
            val kills = this["kills"].asMap { id, amount -> id to amount.asLong(0) }

            return (deaths.keys + kills.keys).toSet().map { id ->
                BestiaryMobData(
                    mobId = id,
                    kills = kills[id] ?: 0,
                    deaths = deaths[id] ?: 0,
                )
            }
        }

        private fun JsonObject.getSlayerData() = this["slayer_bosses"].asMap { n, e -> n to SlayerTypeData.fromJson(e.asJsonObject) }.sortToSlayerOrder()

        private fun JsonObject?.parseEssencePerks(): Map<String, Int> {
            val perks = this?.asMap { id, amount -> id to amount.asInt(0) } ?: emptyMap()

            // perks that are unlocked but not in the repo:
            val unknownPerks = perks.keys - EssenceData.allPerks.keys

            if (unknownPerks.isNotEmpty()) {
                println("Unknown essence perks: $unknownPerks")
                ChatUtils.chat("${unknownPerks.size} Unknown essence perks. Please report this in the discord or the github")
            }

            return perks
        }
    }
}
