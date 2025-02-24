package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.Util
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.api.remote.SkyBlockItems
import tech.thatgravyboat.skyblockpv.api.CollectionAPI
import tech.thatgravyboat.skyblockpv.data.CollectionItem
import tech.thatgravyboat.skyblockpv.data.Currency
import tech.thatgravyboat.skyblockpv.data.MobData
import tech.thatgravyboat.skyblockpv.data.SlayerTypeData
import tech.thatgravyboat.skyblockpv.data.SortedEntries.sortToSkyBlockOrder
import tech.thatgravyboat.skyblockpv.utils.*
import java.util.*

data class SkyblockProfile(
    val selected: Boolean,
    val id: ProfileId,
    val profileType: ProfileType = ProfileType.UNKNOWN,

    val currency: Currency,
    /**Level to Progress*/
    val skyBlockLevel: Pair<Int, Int>,
    val firstJoin: Long,
    val skill: Map<String, Long>,
    val collections: List<CollectionItem>,
    val mobData: List<MobData>,
    val slayer: Map<String, SlayerTypeData>,
) {
    companion object {

        fun fromJson(json: JsonObject, user: UUID): SkyblockProfile? {
            val member = json.getAsJsonObject("members").getAsJsonObject(user.toString().replace("-", "")) ?: return null
            val playerStats = member.getAsJsonObject("player_stats")
            val playerData = member.getAsJsonObject("player_data")
            val profile = member.getAsJsonObject("profile")

            return SkyblockProfile(
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

                currency = run {
                    val currencies = member.getAsJsonObject("currencies") ?: JsonObject()

                    Currency(
                        purse = currencies["coin_purse"].asLong(0),
                        motes = currencies["motes_purse"].asLong(0),
                        mainBank = member["banking"].asLong(0),
                        soloBank = json.getAsJsonObject("banking")?.get("balance").asLong(0),
                        cookieBuffActive = profile["cookie_buff_active"].asBoolean(false),
                    )
                },

                firstJoin = profile["first_join"].asLong(0),
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
    }
}
