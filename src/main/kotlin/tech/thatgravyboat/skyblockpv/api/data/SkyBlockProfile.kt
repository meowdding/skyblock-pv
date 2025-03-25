package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.Util
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.api.remote.SkyBlockItems
import tech.thatgravyboat.skyblockapi.api.remote.UseRepoLib
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockpv.api.CollectionAPI
import tech.thatgravyboat.skyblockpv.data.*
import tech.thatgravyboat.skyblockpv.data.Currency
import tech.thatgravyboat.skyblockpv.data.SortedEntry.Companion.sortToCollectionsOrder
import tech.thatgravyboat.skyblockpv.data.SortedEntry.Companion.sortToSkillsOrder
import tech.thatgravyboat.skyblockpv.data.SortedEntry.Companion.sortToSlayerOrder
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import java.util.*

data class SkyBlockProfile(
    val selected: Boolean,
    val id: ProfileId,
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
) {
    companion object {

        fun fromJson(json: JsonObject, user: UUID): SkyBlockProfile? {
            val member = json.getAsJsonObject("members").getAsJsonObject(user.toString().replace("-", "")) ?: return null
            val playerStats = member.getAsJsonObject("player_stats")
            val playerData = member.getAsJsonObject("player_data")
            val profile = member.getAsJsonObject("profile")

            return SkyBlockProfile(
                selected = json["selected"].asBoolean(false),
                id = ProfileId(
                    id = json["profile_id"].asUUID(Util.NIL_UUID),
                    name = json["cute_name"].asString("Unknown"),
                ),

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
                fairySouls = member.getAsJsonObject("fairy_soul")?.get("total_collected").asInt(0),
                skyBlockLevel = run {
                    val level = member.getAsJsonObject("leveling")
                    val experience = level?.get("experience").asInt(0)

                    experience / 100 to (experience % 100).toInt()
                },

                //  todo: missing skill data when not unlocked
                skill = playerData["experience"].asMap { id, amount -> id to amount.asLong(0) }.sortToSkillsOrder(),
                collections = member.getCollectionData(),
                mobData = playerStats?.getMobData() ?: emptyList(),
                slayer = member.getAsJsonObject("slayer")?.getSlayerData() ?: emptyMap(),
                dungeonData = member.getAsJsonObject("dungeons")?.let { DungeonData.fromJson(it) },
                mining = member.getAsJsonObject("mining_core")?.let { MiningCore.fromJson(it) },
                forge = member.getAsJsonObject("forge")?.let { Forge.fromJson(it) },
                glacite = member.getAsJsonObject("glacite_player_data")?.let { GlaciteData.fromJson(it) },
                tamingLevelPetsDonated = run {
                    val petsData = member.getAsJsonObject("pets_data")
                    val petCare = petsData?.getAsJsonObject("pet_care")
                    val donatedPets = petCare?.getAsJsonArray("pet_types_sacrificed")

                    donatedPets?.toList()?.map { it.asString("") }?.filter { it.isNotBlank() } ?: emptyList()
                },
                pets = member.getAsJsonObject("pets_data").getAsJsonArray("pets").map { Pet.fromJson(it.asJsonObject) },
                trophyFish = TrophyFishData.fromJson(member),
                miscFishData = FishData.fromJson(member, playerStats, playerData),
                essenceUpgrades = playerData?.getAsJsonObject("perks").parseEssencePerks(),
                petMilestones = playerStats?.getAsJsonObject("pets")?.getAsJsonObject("milestone").asMap { id, amount -> id to amount.asInt(0) },
                gardenData = run {
                    val data = member.getAsJsonObject("garden_player_data") ?: return@run GardenData(0, 0, 0)

                    return@run GardenData(
                        data.get("copper").asInt(0),
                        data.get("larva_consumed").asInt(0),
                        playerStats.get("glowing_mushrooms_broken").asInt(0)
                    )
                },
            )
        }

        @OptIn(UseRepoLib::class)
        private fun JsonObject.getCollectionData(): List<CollectionItem> {
            val playerCollections = this["collection"].asMap { id, amount -> id to amount.asLong(0) }
            val allCollections =
                CollectionAPI.collectionData.entries.flatMap { it.value.items.entries }.associate { it.key to it.value }.sortToCollectionsOrder()
            return allCollections.map { (id, _) ->
                id to (playerCollections[id] ?: 0)
            }.mapNotNull { (id, amount) ->
                CollectionAPI.getCategoryByItemName(id)?.let {
                    CollectionItem(it, id, SkyBlockItems.getItemById(id), amount)
                }
            }
        }

        private fun JsonObject.getMobData(): List<MobData> {
            val deaths = this["deaths"].asMap { id, amount -> id to amount.asLong(0) }
            val kills = this["kills"].asMap { id, amount -> id to amount.asLong(0) }

            return (deaths.keys + kills.keys).map { id ->
                MobData(
                    mobId = id,
                    kills = kills[id] ?: 0,
                    deaths = deaths[id] ?: 0,
                )
            }
        }

        private fun JsonObject.getSlayerData() = this["slayer_bosses"].asMap { name, data ->
            val data = data.asJsonObject
            name to SlayerTypeData(
                exp = data["xp"].asLong(0),
                bossAttemptsTier = (0..4).associateWith { tier ->
                    data["boss_attempts_tier_$tier"].asInt(0)
                },
                bossKillsTier = (0..4).associateWith { tier ->
                    data["boss_kills_tier_$tier"].asInt(0)
                },
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
