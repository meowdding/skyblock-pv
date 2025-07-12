package me.owdding.skyblockpv.api.pronouns

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.json.getPathAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asStringList
import tech.thatgravyboat.skyblockapi.utils.http.Http
import java.util.*
import java.util.concurrent.CompletableFuture

private const val API_URL = "https://pronoundb.org/api/v2/lookup"

object PronounsDbAPI {

    private val pronouns = mutableMapOf<UUID, PronounDbData>()

    suspend fun get(uuid: UUID) = pronouns.getOrPut(uuid) {
        try {

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
                val decoration = data.getAs<String>("decoration")
                val pronouns = data.getPathAs<JsonArray>("sets.en")?.asStringList()?.mapNotNull(ProunounSet::fromId) ?: emptyList()
                PronounDbData(decoration, pronouns)
            } ?: PronounDbData()
        } catch (e: Exception) {
            e.printStackTrace()
            PronounDbData() // Return empty data on error
        }
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

enum class ProunounSet(val id: String, val first: String, val second: String) {
    UNKNOWN("unknown", "?", "?"),

    HE_HIM("he", "he", "him"),
    IT_ITS("it", "it", "its"),
    SHE_HER("she", "she", "her"),
    THEY_THEM("they", "they", "them"),

    ANY("any", "any", "any"),
    ASK("ask", "ask", "ask"),
    AVOID("avoid", "avoid", "avoid"),
    OTHER("other", "other", "other")
    ;

    companion object {
        fun fromId(id: String) = entries.firstOrNull { it.id == id }
    }
}
