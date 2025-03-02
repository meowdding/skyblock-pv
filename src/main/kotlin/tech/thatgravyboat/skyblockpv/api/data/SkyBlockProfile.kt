package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.Util
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.api.remote.SkyBlockItems
import tech.thatgravyboat.skyblockpv.api.CollectionAPI
import tech.thatgravyboat.skyblockpv.data.*
import tech.thatgravyboat.skyblockpv.data.Currency
import tech.thatgravyboat.skyblockpv.data.SortedEntries.sortToSkyBlockOrder
import tech.thatgravyboat.skyblockpv.utils.*
import java.util.*

data class SkyBlockProfile(
    val selected: Boolean,
    val id: ProfileId,
    val profileType: ProfileType = ProfileType.UNKNOWN,

    val currency: Currency,
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

                inventory = member.getAsJsonObject("inventory")?.getInventory(),

                currency = run {
                    val currencies = member.getAsJsonObject("currencies") ?: JsonObject()

                    Currency(
                        purse = currencies["coin_purse"].asLong(0),
                        motes = currencies["motes_purse"].asLong(0),
                        mainBank = member["banking"].asLong(0),
                        soloBank = json.getAsJsonObject("banking")?.get("balance").asLong(0),
                        cookieBuffActive = profile["cookie_buff_active"].asBoolean(false),
                        essence = currencies["essence"].asMap { id, obj -> id to obj.asJsonObject["current"].asLong(0) },
                    )
                },

                firstJoin = profile["first_join"].asLong(0),
                fairySouls = member.getAsJsonObject("fairy_soul")?.get("total_collected").asInt(0),
                skyBlockLevel = run {
                    val level = member.getAsJsonObject("leveling")
                    val experience = level["experience"].asInt(0)

                    experience / 100 to (experience % 100).toInt()
                },

                //  todo: missing skill data when not unlocked
                skill = playerData["experience"].asMap { id, amount -> id to amount.asLong(0) }.sortToSkyBlockOrder(),
                collections = member.getCollectionData(),
                mobData = playerStats?.getMobData() ?: emptyList(),
                slayer = member.getAsJsonObject("slayer")?.getSlayerData() ?: emptyMap(),
                dungeonData = member.getAsJsonObject("dungeons")?.parseDungeonData(),
                mining = member.getAsJsonObject("mining_core")?.parseMiningData(),
            )
        }

        private fun JsonObject.parseMiningData(): MiningCore {
            val nodes = this.getAsJsonObject("nodes").asMap { id, amount -> id to amount.asInt(0) }
            val crystals = this.getAsJsonObject("crystals").asMap { id, data ->
                val obj = data.asJsonObject
                id to Crystal(
                    state = obj["state"].asString(""),
                    totalPlaced = obj["total_placed"].asInt(0),
                    totalFound = obj["total_found"].asInt(0),
                )
            }

            return MiningCore(
                nodes = nodes,
                crystals = crystals,
                experience = this["experience"].asLong(0),
                powderMithril = this["powder_mithril"].asInt(0),
                powderSpentMithril = this["powder_spent_mithril"].asInt(0),
                powderGemstone = this["powder_gemstone"].asInt(0),
                powderSpentGemstone = this["powder_spent_gemstone"].asInt(0),
                powderGlacite = this["powder_glacite"].asInt(0),
                powderSpentGlacite = this["powder_spent_glacite"].asInt(0),
            )
        }

        private fun JsonObject.parseDungeonData(): DungeonData {
            val dungeonsTypes = this.getAsJsonObject("dungeon_types")
            val catacombs = dungeonsTypes.getAsJsonObject("catacombs").parseDungeonType()
            val catacombsMaster = dungeonsTypes.getAsJsonObject("master_catacombs").parseDungeonType()
            val classExperience = this.getAsJsonObject("player_classes").parseClassExperience()
            val secrets = this["secrets"].asLong(0)

            return DungeonData(
                dungeonTypes = mapOf(
                    "catacombs" to catacombs,
                    "master_catacombs" to catacombsMaster,
                ),
                classExperience = classExperience,
                secrets = secrets,
            )
        }

        private fun JsonObject.parseClassExperience() = this.asMap { id, data ->
            id to data.asJsonObject["experience"].asLong(0)
        }

        private fun JsonObject.parseDungeonType(): DungeonTypeData {
            val timesPlayed = this["times_played"].asMap { id, amount -> id to amount.asLong(0) }
            val tierCompletions = this["tier_completions"].asMap { id, amount -> id to amount.asLong(0) }
            val fastestTime = this["fastest_time"].asMap { id, amount -> id to amount.asLong(0) }
            val bestScore = this["best_score"].asMap { id, amount -> id to amount.asLong(0) }
            val experience = this["experience"].asLong(0)

            return DungeonTypeData(
                timesPlayed = timesPlayed,
                tierCompletions = tierCompletions,
                fastestTime = fastestTime,
                bestScore = bestScore,
                experience = experience,
            )
        }

        private fun JsonObject.getCollectionData(): List<CollectionItem> {
            val playerCollections = this["collection"].asMap { id, amount -> id to amount.asLong(0) }
            val allCollections = CollectionAPI.collectionData.entries.flatMap { it.value.items.entries }.associate { it.key to it.value }.sortToSkyBlockOrder()
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
        }.sortToSkyBlockOrder()

        private fun JsonObject.getInventory(): InventoryData {
            val backpackIcons: Map<Int, ItemStack> = InventoryData.Backpack.icons(this.getAsJsonObject("backpack_icons"))
            val bagContents = this.getAsJsonObject("bag_contents")
            return InventoryData(
                inventoryItems = this.getAsJsonObject("inv_contents")?.let { InventoryData.Inventory.fromJson(it) },
                enderChestPages = this.getAsJsonObject("ender_chest_contents")?.let { InventoryData.EnderChestPage.fromJson(it) },
                potionBag = bagContents.getAsJsonObject("potion_bag")?.let { InventoryData.Inventory.fromJson(it) },
                talismans = bagContents.getAsJsonObject("talisman_bag")?.let { InventoryData.TalismansPage.fromJson(it) },
                fishingBag = bagContents.getAsJsonObject("fishing_bag")?.let { InventoryData.Inventory.fromJson(it) },
                sacks = bagContents.getAsJsonObject("sacks")?.let { InventoryData.Inventory.fromJson(it) },
                quiver = bagContents.getAsJsonObject("quiver")?.let { InventoryData.Inventory.fromJson(it) },
                armorItems = this.getAsJsonObject("inv_armor")?.let { InventoryData.Inventory.fromJson(it) },
                equipmentItems = this.getAsJsonObject("equipment_contents")?.let { InventoryData.Inventory.fromJson(it) },
                personalVault = this.getAsJsonObject("personal_vault_contents")?.let { InventoryData.Inventory.fromJson(it) },
                backpacks = this.getAsJsonObject("backpack_contents")?.let {
                    InventoryData.Backpack.fromJson(it).map { (id, inv) ->
                        InventoryData.Backpack(items = inv, icon = backpackIcons[id] ?: ItemStack.EMPTY)
                    }
                },
                wardrobe = this.getAsJsonObject("wardrobe_contents")?.getAsJsonObject("armor")?.let {
                    InventoryData.Wardrobe(
                        equippedArmor = this.get("wardrobe_equipped_slot").asInt,
                        armor = InventoryData.Wardrobe.fromJson(it),
                    )
                },
            )
        }
    }
}
