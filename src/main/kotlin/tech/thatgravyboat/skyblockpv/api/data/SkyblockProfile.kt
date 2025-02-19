package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.Util
import tech.thatgravyboat.skyblockapi.api.remote.SkyBlockItems
import tech.thatgravyboat.skyblockpv.data.CollectionCategory
import tech.thatgravyboat.skyblockpv.data.CollectionItem
import tech.thatgravyboat.skyblockpv.data.MobData
import tech.thatgravyboat.skyblockpv.utils.*
import java.util.*

data class SkyblockProfile(
    val selected: Boolean,
    val id: ProfileId,

    val collections: List<CollectionItem>,
    val mobData: List<MobData>,
) {
    companion object {

        fun fromJson(json: JsonObject, user: UUID): SkyblockProfile? {
            val member = json.getAsJsonObject("members").getAsJsonObject(user.toString().replace("-", "")) ?: return null

            return SkyblockProfile(
                selected = json["selected"].asBoolean(false),
                id = ProfileId(
                    id = json["profile_id"].asUUID(Util.NIL_UUID),
                    name = json["cute_name"].asString("Unknown"),
                ),

                collections = member["collection"].asMap { string, element -> string to element.asLong(0) }.mapNotNull { (id, amount) ->
                    CollectionCategory.getCategoryByItemName(id)?.let { CollectionItem(it, id, SkyBlockItems.getItemById(id), amount) }
                },

                mobData = run {
                    val playerStats = member.getAsJsonObject("player_stats") ?: return@run emptyList()
                    val deaths = playerStats["deaths"].asMap { id, amount -> id to amount.asLong(0) }
                    val kills = playerStats["kills"].asMap { id, amount -> id to amount.asLong(0) }

                    (deaths.keys + kills.keys).map { id ->
                        MobData(
                            mobId = id,
                            kills = kills[id] ?: 0,
                            deaths = deaths[id] ?: 0,
                        )
                    }
                },
            )
        }
    }
}
