package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject

data class PlayerStatus(
    val status: Status,
    val location: String?,
    val gameType: String?,
    val map: String?
) {

    companion object {
        fun fromJson(json: JsonObject): PlayerStatus {
            if (json.has("success") && json.get("success").asBoolean) {
                val session = json.getAsJsonObject("session")
                val online = session.get("online")?.asBoolean
                val location = session.get("mode")?.asString
                val gameType = session.get("gameType")?.asString
                val map = session.get("map")?.asString
                return PlayerStatus(if (online == true) Status.ONLINE else Status.OFFLINE, location, gameType, map)
            }
            return PlayerStatus(Status.ERROR, null, null, null)
        }
    }

    enum class Status {
        ONLINE,
        OFFLINE,
        ERROR
    }
}
