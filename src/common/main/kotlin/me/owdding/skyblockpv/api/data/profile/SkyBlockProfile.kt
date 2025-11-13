package me.owdding.skyblockpv.api.data.profile

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.CollectionAPI
import me.owdding.skyblockpv.api.SkillAPI
import me.owdding.skyblockpv.api.data.InventoryData
import me.owdding.skyblockpv.api.data.ProfileId
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
import me.owdding.skyblockpv.data.repo.MagicalPowerCodecs
import me.owdding.skyblockpv.feature.networth.Networth
import me.owdding.skyblockpv.feature.networth.NetworthCalculator
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.toDashlessString
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.json.getPathAs
import net.minecraft.Util
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.remote.SkyBlockPvOpenedEvent
import tech.thatgravyboat.skyblockapi.api.events.remote.SkyBlockPvRequired
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

interface SkyBlockProfile {
    val selected: Boolean get() = backingProfile.selected
    val id: ProfileId get() = backingProfile.id
    val userId: UUID get() = backingProfile.userId
    val profileType: ProfileType get() = backingProfile.profileType.getNowOrElse(ProfileType.UNKNOWN)

    val currency: Currency? get() = backingProfile.currency.getNowOrElse(null)
    val bank: Bank? get() = backingProfile.bank.getNowOrElse(null)
    val inventory: InventoryData? get() = backingProfile.inventory.getNowOrElse(null)

    /**Level to Progress*/
    val skyBlockLevel: Pair<Int, Int> get() = backingProfile.skyBlockLevel.getNowOrElse(0 to 0)
    val firstJoin: Long get() = backingProfile.firstJoin
    val fairySouls: Int get() = backingProfile.fairySouls
    val skill: Map<String, Long> get() = backingProfile.skill.getNowOrElse(emptyMap())
    val collections: List<CollectionItem>? get() = backingProfile.collections.getNowOrElse(emptyList())
    val mobData: List<MobData> get() = backingProfile.mobData.getNowOrElse(emptyList())
    val bestiaryData: List<BestiaryMobData> get() = backingProfile.bestiaryData.getNowOrElse(emptyList())
    val slayer: Map<String, SlayerTypeData> get() = backingProfile.slayer.getNowOrElse(emptyMap())
    val dungeonData: DungeonData? get() = backingProfile.dungeonData.getNowOrElse(null)
    val mining: MiningCore? get() = backingProfile.mining.getNowOrElse(null)
    val forge: Forge? get() = backingProfile.forge.getNowOrElse(null)
    val glacite: GlaciteData? get() = backingProfile.glacite.getNowOrElse(null)
    val tamingLevelPetsDonated: List<String> get() = backingProfile.tamingLevelPetsDonated.getNowOrElse(emptyList())
    val pets: List<Pet> get() = backingProfile.pets.getNowOrElse(emptyList())
    val petMilestones: Map<String, Int> get() = backingProfile.petMilestones.getNowOrElse(emptyMap())
    val trophyFish: TrophyFishData get() = backingProfile.trophyFish.getNowOrElse(TrophyFishData.EMPTY)
    val miscFishData: FishData get() = backingProfile.miscFishData.getNowOrElse(FishData.EMPTY)
    val essenceUpgrades: Map<String, Int> get() = backingProfile.essenceUpgrades.getNowOrElse(emptyMap())
    val gardenData: GardenData get() = backingProfile.gardenData.getNowOrElse(GardenData.EMPTY)
    val farmingData: FarmingData get() = backingProfile.farmingData.getNowOrElse(FarmingData.EMPTY)
    val chocolateFactoryData: CfData? get() = backingProfile.chocolateFactoryData.getNowOrElse(null)
    val rift: RiftData? get() = backingProfile.rift.getNowOrElse(null)
    val crimsonIsleData: CrimsonIsleData get() = backingProfile.crimsonIsleData.getNowOrElse(CrimsonIsleData.EMPTY)
    val minions: List<String>? get() = backingProfile.minions.getNowOrElse(emptyList())
    val maxwell: Maxwell? get() = backingProfile.maxwell.getNowOrElse(null)

    val backingProfile: BackingSkyBlockProfile
    val dataFuture: CompletableFuture<Void> get() = backingProfile.dataFuture

    val netWorth: CompletableFuture<Networth>
    val magicalPower: Pair<Int, Component>

    val onStranded: Boolean get() = profileType == ProfileType.STRANDED
    val isOwnProfile get() = userId == McPlayer.uuid && selected

    val isEmpty: Boolean

    companion object {
        fun fromJson(json: JsonObject, user: UUID) = BackingSkyBlockProfile.fromJson(json, user)?.let(::CompletableSkyBlockProfile)
    }
}

private fun <T> CompletableFuture<T>.getNowOrElse(defaultValue: T) = if (this.isCompletedExceptionally) defaultValue else this.getNow(defaultValue)

data class CompletableSkyBlockProfile(override val backingProfile: BackingSkyBlockProfile) : SkyBlockProfile {
    override val netWorth: CompletableFuture<Networth> = NetworthCalculator.calculateNetworthAsync(this)
    override val magicalPower: Pair<Int, Component> = MagicalPowerCodecs.calculateMagicalPower(this)
    override val isEmpty: Boolean get() = false
}

data class BackingSkyBlockProfile(
    val selected: Boolean,
    val id: ProfileId,
    val userId: UUID,
    val profileType: CompletableFuture<ProfileType> = emptyFuture(),

    val currency: CompletableFuture<Currency?> = emptyFuture(),
    val bank: CompletableFuture<Bank?> = emptyFuture(),
    val inventory: CompletableFuture<InventoryData?> = emptyFuture(),
    /**Level to Progress*/
    val skyBlockLevel: CompletableFuture<Pair<Int, Int>> = emptyFuture(),
    val firstJoin: Long = 0,
    val fairySouls: Int = 0,
    val skill: CompletableFuture<Map<String, Long>> = emptyFuture(),
    val collections: CompletableFuture<List<CollectionItem>?> = emptyFuture(),
    val mobData: CompletableFuture<List<MobData>> = emptyFuture(),
    val bestiaryData: CompletableFuture<List<BestiaryMobData>> = emptyFuture(),
    val slayer: CompletableFuture<Map<String, SlayerTypeData>> = emptyFuture(),
    val dungeonData: CompletableFuture<DungeonData?> = emptyFuture(),
    val mining: CompletableFuture<MiningCore?> = emptyFuture(),
    val forge: CompletableFuture<Forge?> = emptyFuture(),
    val glacite: CompletableFuture<GlaciteData?> = emptyFuture(),
    val tamingLevelPetsDonated: CompletableFuture<List<String>> = emptyFuture(),
    val pets: CompletableFuture<List<Pet>> = emptyFuture(),
    val petMilestones: CompletableFuture<Map<String, Int>> = emptyFuture(),
    val trophyFish: CompletableFuture<TrophyFishData> = emptyFuture(),
    val miscFishData: CompletableFuture<FishData> = emptyFuture(),
    val essenceUpgrades: CompletableFuture<Map<String, Int>> = emptyFuture(),
    val gardenData: CompletableFuture<GardenData> = emptyFuture(),
    val farmingData: CompletableFuture<FarmingData> = emptyFuture(),
    val chocolateFactoryData: CompletableFuture<CfData?> = emptyFuture(),
    val rift: CompletableFuture<RiftData?> = emptyFuture(),
    val crimsonIsleData: CompletableFuture<CrimsonIsleData> = emptyFuture(),
    val minions: CompletableFuture<List<String>?> = emptyFuture(),
    val maxwell: CompletableFuture<Maxwell?> = emptyFuture(),
) {
    val dataFuture: CompletableFuture<Void> = CompletableFuture.allOf(
        profileType,
        currency,
        bank,
        inventory,
        skyBlockLevel,
        skill,
        collections,
        mobData,
        bestiaryData,
        slayer,
        dungeonData,
        mining,
        forge,
        glacite,
        tamingLevelPetsDonated,
        pets,
        petMilestones,
        trophyFish,
        miscFishData,
        essenceUpgrades,
        gardenData,
        farmingData,
        chocolateFactoryData,
        rift,
        crimsonIsleData,
        minions,
        maxwell,
    )

    companion object {

        private val executorPool = Executors.newFixedThreadPool(12)

        private fun <T> emptyFuture(): CompletableFuture<T> = CompletableFuture()

        @OptIn(SkyBlockPvRequired::class)
        fun fromJson(json: JsonObject, user: UUID): BackingSkyBlockProfile? {
            val member = json.getPath("members.${user.toDashlessString()}")?.asJsonObject ?: return null
            val playerStats = member.getAs<JsonObject>("player_stats")
            val playerData = member.getAs<JsonObject>("player_data")
            val profile = member.getAs<JsonObject>("profile")

            fun <T, R> allMembers(extractor: (member: JsonObject) -> T, adder: (members: List<T>) -> R): R {
                val members =
                    json.getAs<JsonObject>("members")
                        ?.entrySet()
                        ?.map { (_, v) -> v }
                        ?.filterIsInstance<JsonObject>()
                        ?.mapNotNull(extractor) ?: emptyList()
                return adder(members)
            }

            fun <T> allMembers(extractor: (member: JsonObject) -> T) = allMembers(extractor) { members -> members }

            val selected = json.getAs<Boolean>("selected", false)
            if (selected && user == McPlayer.uuid) {
                SkyBlockPvOpenedEvent(json).post(SkyBlockAPI.eventBus)
            }

            return BackingSkyBlockProfile(
                selected = selected,
                id = ProfileId(
                    id = json.getAs("profile_id", Util.NIL_UUID),
                    name = json.getAs("cute_name", "Unknown"),
                ),
                userId = user,
                profileType = future {
                    when (json.getAs<String>("game_mode")) {
                        "ironman" -> ProfileType.IRONMAN
                        "island" -> ProfileType.STRANDED
                        "bingo" -> ProfileType.BINGO
                        else -> ProfileType.NORMAL
                    }
                },
                inventory = future { member.getAs<JsonObject>("inventory")?.let { InventoryData.fromJson(it, member.getAsJsonObject("shared_inventory")) } },
                currency = future { Currency.fromJson(member) },
                bank = future { Bank.fromJson(json, member) },
                firstJoin = profile.getAs<Long>("first_join", 0L),
                fairySouls = member.getPathAs<Int>("fairy_soul.total_collected", 0),
                skyBlockLevel = future {
                    val experience = member.getPathAs<Int>("leveling.experience", 0)

                    experience / 100 to (experience % 100)
                },
                skill = playerData.getSkillData(),
                collections = future {
                    allMembers(::getCollectionData) { members ->
                        members.filterNotNull().flatten().groupBy { it.itemId }.values.mapNotNull { items ->
                            val item = items.firstOrNull() ?: return@mapNotNull null
                            CollectionItem(item.category, item.itemId, items.sumOf { (_, _, count) -> count })
                        }
                    }.takeUnless { it.isEmpty() }
                },
                mobData = future { playerStats?.getMobData() ?: emptyList() },
                bestiaryData = future { member.getPath("bestiary")?.asJsonObject?.getBestiaryMobData() ?: emptyList() },
                slayer = future { member.getAs<JsonObject>("slayer")?.getSlayerData() ?: emptyMap() },
                dungeonData = future { member.getAs<JsonObject>("dungeons")?.let { DungeonData.fromJson(it) } },
                mining = future { member.getAs<JsonObject>("mining_core")?.let { MiningCore.fromJson(it) } },
                forge = future { member.getAs<JsonObject>("forge")?.let { Forge.fromJson(it) } },
                glacite = future { member.getAs<JsonObject>("glacite_player_data")?.let { GlaciteData.fromJson(it) } },
                tamingLevelPetsDonated = future { member.getPath("pets_data.pet_care.pet_types_sacrificed").asStringList().filter { it.isNotBlank() } },
                pets = future { member.getPathAs<JsonArray>("pets_data.pets").asList { Pet.fromJson(it.asJsonObject) } },
                trophyFish = future { TrophyFishData.fromJson(member) },
                miscFishData = future { FishData.fromJson(member, playerStats, playerData) },
                essenceUpgrades = future { playerData?.getAs<JsonObject>("perks").parseEssencePerks() },
                petMilestones = future { playerStats?.getPath("pets.milestone").asMap { id, amount -> id to amount.asInt(0) } },
                gardenData = future {
                    val data = member.getAs<JsonObject>("garden_player_data") ?: return@future GardenData(0, 0, 0)

                    GardenData(
                        data.getAs<Int>("copper", 0),
                        data.getAs<Int>("larva_consumed", 0),
                        playerStats.getAs<Int>("glowing_mushrooms_broken", 0),
                    )
                },
                farmingData = future { FarmingData.fromJson(member.getAs("jacobs_contest")) },
                chocolateFactoryData = future { member.getPath("events.easter")?.let { CfData.fromJson(it.asJsonObject) } },
                rift = future {
                    playerStats?.getAs<JsonObject>("rift")
                        ?.let { stats -> member.getAs<JsonObject>("rift")?.let { RiftData.fromJson(it, stats) } }
                },
                crimsonIsleData = future { CrimsonIsleData.fromJson(member.getAs("nether_island_player_data")) },
                minions = future {
                    allMembers {
                        it.getPathAs<JsonArray>("player_data.crafted_generators")?.asStringList()
                            ?.filter { it.isNotBlank() }
                            ?.sortedByDescending { it.filter { it.isDigit() }.toIntOrNull() ?: -1 }
                    }.mapNotNull { it }.flatten()
                },
                maxwell = future { member.getAs<JsonObject>("accessory_bag_storage")?.let { Maxwell.fromJson(member, it) } },
            )
        }

        private fun JsonObject?.getSkillData(): CompletableFuture<Map<String, Long>> = future {
            val skills = this?.getAs<JsonElement>("experience").asMap { id, amount -> id to amount.asLong(0) }
                .filterKeys { it != "SKILL_DUNGEONEERING" }
                .toMutableMap()

            SkillAPI.Skills.entries.forEach { skill ->
                skills.putIfAbsent(skill.skillApiId, 0)
            }

            skills.sortToSkillsOrder()
        }

        private fun getCollectionData(data: JsonObject): List<CollectionItem>? = with(data) {
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

        private fun JsonObject.getSlayerData() =
            this["slayer_bosses"].asMap { n, e -> n to SlayerTypeData.fromJson(e.asJsonObject) }.sortToSlayerOrder()

        private fun JsonObject?.parseEssencePerks(): Map<String, Int> {
            val perks = this?.asMap { id, amount -> id to amount.asInt(0) } ?: emptyMap()

            // perks that are unlocked but not in the repo:
            val unknownPerks = perks.keys - EssenceData.allPerks.keys

            if (unknownPerks.isNotEmpty()) {
                McClient.runNextTick {
                    SkyBlockPv.warn("Unknown essence perks: $unknownPerks")
                    if (SkyBlockPv.isDevMode) "messages.unknown_essence_perks".asTranslated(unknownPerks.size).sendWithPrefix()
                }
            }

            return perks
        }

        fun <T> future(supplier: () -> T): CompletableFuture<T> = CompletableFuture.supplyAsync(supplier, executorPool)
    }
}
