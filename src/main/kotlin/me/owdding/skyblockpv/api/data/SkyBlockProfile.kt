package me.owdding.skyblockpv.api.data

import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.CollectionAPI
import me.owdding.skyblockpv.api.SkillAPI
import me.owdding.skyblockpv.data.SortedEntry.Companion.sortToCollectionsOrder
import me.owdding.skyblockpv.data.SortedEntry.Companion.sortToSkillsOrder
import me.owdding.skyblockpv.data.SortedEntry.Companion.sortToSlayerOrder
import me.owdding.skyblockpv.data.api.CfData
import me.owdding.skyblockpv.data.api.CollectionItem
import me.owdding.skyblockpv.data.api.Currency
import me.owdding.skyblockpv.data.api.RiftData
import me.owdding.skyblockpv.data.api.skills.*
import me.owdding.skyblockpv.data.api.skills.combat.*
import me.owdding.skyblockpv.data.api.skills.farming.FarmingData
import me.owdding.skyblockpv.data.api.skills.farming.GardenData
import me.owdding.skyblockpv.data.repo.EssenceData
import me.owdding.skyblockpv.utils.ChatUtils
import me.owdding.skyblockpv.utils.Utils.toDashlessString
import net.minecraft.Util
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.util.*

data class SkyBlockProfile(
    val selected: Boolean,
    val id: ProfileId,
    val userId: UUID,
    val profileType: ProfileType = ProfileType.UNKNOWN,

    val currency: Currency?,
    val inventory: InventoryData?,
    /**Level to Progress*/
    val skyBlockLevel: Pair<Int, Int>,
    val firstJoin: Long,
    val fairySouls: Int,
    val skill: Map<String, Long>,
    val collections: List<CollectionItem>,
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
    val minions: List<String>,
) {
    companion object {

        fun fromJson(json: JsonObject, user: UUID): SkyBlockProfile? {
            val member = json.getPath("members.${user.toDashlessString()}")?.asJsonObject ?: return null
            val playerStats = member.getAsJsonObject("player_stats")
            val playerData = member.getAsJsonObject("player_data")
            val profile = member.getAsJsonObject("profile")

            return SkyBlockProfile(
                selected = json["selected"].asBoolean(false),
                id = ProfileId(
                    id = json["profile_id"].asUUID(Util.NIL_UUID),
                    name = json["cute_name"].asString("Unknown"),
                ),
                userId = user,

                profileType = json.get("game_mode")?.asString.let {
                    when (it) {
                        "ironman" -> ProfileType.IRONMAN
                        "island" -> ProfileType.STRANDED
                        "bingo" -> ProfileType.BINGO
                        else -> ProfileType.NORMAL
                    }
                },

                inventory = member.getAsJsonObject("inventory")?.let { InventoryData.fromJson(it) },
                currency = member.getAsJsonObject("currencies")?.let { Currency.fromJson(it) },
                firstJoin = profile["first_join"].asLong(0),
                fairySouls = member.getPath("fairy_soul.total_collected").asInt(0),
                skyBlockLevel = run {
                    val level = member.getAsJsonObject("leveling")
                    val experience = level?.get("experience").asInt(0)

                    experience / 100 to (experience % 100)
                },
                skill = playerData.getSkillData(),
                collections = member.getCollectionData(),
                mobData = playerStats?.getMobData() ?: emptyList(),
                bestiaryData = member.getPath("bestiary")?.asJsonObject?.getBestiaryMobData() ?: emptyList(),
                slayer = member.getAsJsonObject("slayer")?.getSlayerData() ?: emptyMap(),
                dungeonData = member.getAsJsonObject("dungeons")?.let { DungeonData.fromJson(it) },
                mining = member.getAsJsonObject("mining_core")?.let { MiningCore.fromJson(it) },
                forge = member.getAsJsonObject("forge")?.let { Forge.fromJson(it) },
                glacite = member.getAsJsonObject("glacite_player_data")?.let { GlaciteData.fromJson(it) },
                tamingLevelPetsDonated = member.getPath("pets_data.pet_care.pet_types_sacrificed").asStringList().filter { it.isNotBlank() },
                pets = member.getAsJsonObject("pets_data").getAsJsonArray("pets").map { Pet.fromJson(it.asJsonObject) },
                trophyFish = TrophyFishData.fromJson(member),
                miscFishData = FishData.fromJson(member, playerStats, playerData),
                essenceUpgrades = playerData?.getAsJsonObject("perks").parseEssencePerks(),
                petMilestones = playerStats?.getPath("pets.milestone").asMap { id, amount -> id to amount.asInt(0) },
                gardenData = run {
                    val data = member.getAsJsonObject("garden_player_data") ?: return@run GardenData(0, 0, 0)

                    return@run GardenData(
                        data.get("copper").asInt(0),
                        data.get("larva_consumed").asInt(0),
                        playerStats.get("glowing_mushrooms_broken").asInt(0),
                    )
                },
                farmingData = FarmingData.fromJson(member.getAsJsonObject("jacobs_contest")),
                chocolateFactoryData = member.getPath("events.easter")?.let { CfData.fromJson(it.asJsonObject) },
                rift = playerStats?.getAsJsonObject("rift")?.let { stats -> RiftData.fromJson(member.getAsJsonObject("rift"), stats) },
                crimsonIsleData = CrimsonIsleData.fromJson(member.getAsJsonObject("nether_island_player_data")),
                minions = playerData?.getAsJsonArray("crafted_generators").asStringList().filterNot { it.isNotBlank() }
                    .sortedByDescending { it.filter { it.isDigit() }.toIntOrNull() ?: -1 },
            )
        }

        private fun JsonObject.getSkillData(): Map<String, Long> {
            val skills = this["experience"].asMap { id, amount -> id to amount.asLong(0) }
                .filterKeys { it != "SKILL_DUNGEONEERING" }
                .toMutableMap()

            SkillAPI.Skills.entries.forEach { skill ->
                skills.putIfAbsent(skill.skillApiId, 0)
            }

            return skills.sortToSkillsOrder()
        }

        private fun JsonObject.getCollectionData(): List<CollectionItem> {
            val playerCollections = this["collection"].asMap { id, amount -> id to amount.asLong(0) }
            val allCollections =
                CollectionAPI.collectionData.entries.flatMap { it.value.items.entries }.associate { it.key to it.value }.sortToCollectionsOrder()
            return allCollections.map { (id, _) ->
                id to (playerCollections[id] ?: 0)
            }.mapNotNull { (id, amount) ->
                CollectionAPI.getCategoryByItemName(id)?.let {
                    CollectionItem(it, id, amount)
                }
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

        private fun JsonObject.getSlayerData() = this["slayer_bosses"].asMap { name, jsonElement ->
            val data = jsonElement.asJsonObject
            name to SlayerTypeData(
                exp = data["xp"].asLong(0),
                bossAttemptsTier = data.entrySet().filter { it.key.startsWith("boss_attempts_tier_") }
                    .associate { it.key.filter { it.isDigit() }.toInt() to it.value.asInt },
                bossKillsTier = data.entrySet().filter { it.key.startsWith("boss_kills_tier_") }
                    .associate { it.key.filter { it.isDigit() }.toInt() to it.value.asInt },
            )
        }.sortToSlayerOrder()

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
