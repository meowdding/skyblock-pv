package me.owdding.skyblockpv.api.pronouns

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import me.owdding.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockapi.utils.extentions.asStringList
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.util.*
import java.util.concurrent.CompletableFuture

private const val API_URL = "https://pronoundb.org/api/v2/lookup"

object PronounsDbAPI {

    private val pronouns = mutableMapOf<UUID, PronounDbData>()

    suspend fun get(uuid: UUID) = pronouns.getOrPut(uuid) {
        val id = uuid.toString()
        val result = Http.getResult<JsonObject>(
            url = API_URL,
            headers = mapOf("User-Agent" to SkyBlockPv.useragent),
            queries = mapOf(
                "ids" to id,
                "platform" to "minecraft",
            ),
        ).getOrNull()

        result?.getAsJsonObject(id)?.let { data ->
            val decoration = data["decoration"]?.asString
            val pronouns = data.getPath("sets.en")?.asStringList()?.mapNotNull(ProunounSet::fromId) ?: emptyList()
            PronounDbData(decoration, pronouns)
        } ?: PronounDbData()
    }

    fun getProunouns(uuid: UUID): PronounDbData {
        return runBlocking { get(uuid) }
    }

    fun getPronounsAsync(uuid: UUID): CompletableFuture<PronounDbData> {
        return CompletableFuture.supplyAsync { getProunouns(uuid) }
    }
}

data class PronounDbData(
    val decoration: String? = null,
    val pronouns: List<ProunounSet> = emptyList(),
)

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
        fun fromId(id: String) = entries.firstOrNull { it.id == id }
    }
}
