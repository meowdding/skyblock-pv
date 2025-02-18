package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.Util
import tech.thatgravyboat.skyblockpv.utils.asBoolean
import tech.thatgravyboat.skyblockpv.utils.asLong
import tech.thatgravyboat.skyblockpv.utils.asMap
import tech.thatgravyboat.skyblockpv.utils.asString
import tech.thatgravyboat.skyblockpv.utils.asUUID
import java.util.*

data class SkyblockProfile(
    val selected: Boolean,
    val id: ProfileId,

    val collections: Map<String, Long>,

) {

    companion object {

        fun fromJson(json: JsonObject, user: UUID): SkyblockProfile? {
            val member = json.getAsJsonObject("members").getAsJsonObject(user.toString().replace("-", "")) ?: return null

            return SkyblockProfile(
                selected = json["selected"].asBoolean(false),
                id = ProfileId(
                    id = json["profile_id"].asUUID(Util.NIL_UUID),
                    name = json["cute_name"].asString("Unknown")
                ),

                collections = member["collection"].asMap { string, element -> string to element.asLong(0) }
            )
        }
    }
}