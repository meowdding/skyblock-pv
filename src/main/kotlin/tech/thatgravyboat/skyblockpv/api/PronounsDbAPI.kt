package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockpv.utils.asList
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import java.util.*

private const val API_URL = "https://pronoundb.org/api/v2/lookup"

object PronounsDbAPI {
    private val pronouns = mutableMapOf<UUID, List<ProunounSet>>()

    suspend fun get(uuid: UUID) = pronouns.getOrPut(uuid) {
        val id = uuid.toString()
        val result = Http.getResult<JsonObject>(
            url = API_URL,
            queries = mapOf(
                "ids" to id,
                "platform" to "minecraft",
            ),
        ).getOrNull()

        result?.getAsJsonObject(id)
            ?.getAsJsonObject("sets")
            ?.getAsJsonArray("en")
            .asList(JsonElement::getAsString)
            .mapNotNull(ProunounSet::fromId)
    }

    fun getDisplay(uuid: UUID): Display {
        var pronounsDisplay = Displays.text("Loading...", color = { 0x555555u }, shadow = false)
        Thread.startVirtualThread {
            val pronouns = runBlocking { get(uuid) }.firstOrNull() ?: ProunounSet.UNKNOWN
            McClient.tell {
                pronounsDisplay = Displays.text(pronouns.toDisplay(), color = { 0x555555u }, shadow = false)
            }
        }

        return Displays.supplied { pronounsDisplay }
    }
}

enum class ProunounSet(val id: String) {
    UNKNOWN("unknown"),

    HE_HIM("he"),
    IT_ITS("it"),
    SHE_HER("she"),
    THEY_THEM("they"),

    ANY("any"),
    ASK("ask"),
    AVOID("avoid"),
    OTHER("other"),
    ;

    fun toDisplay() = this.name.lowercase().replace("_", "/")

    companion object {
        fun fromId(id: String) = ProunounSet.entries.firstOrNull { it.id == id }
    }
}
