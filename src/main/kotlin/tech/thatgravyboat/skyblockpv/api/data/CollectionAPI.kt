package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockpv.data.CollectionCategory
import tech.thatgravyboat.skyblockpv.data.CollectionEntry
import tech.thatgravyboat.skyblockpv.utils.asInt
import tech.thatgravyboat.skyblockpv.utils.asLong
import tech.thatgravyboat.skyblockpv.utils.asString

private const val API_URL = "https://api.hypixel.net/v2/resources/skyblock/collections"

object CollectionAPI {
    var collectionData: Map<String, CollectionCategory> = emptyMap()
        private set

    fun getCategoryByItemName(id: String) = collectionData.entries.find { it.value.items.containsKey(id) }?.key

    fun getCollectionEntry(id: String) = collectionData.entries.find { it.value.items.containsKey(id) }?.value?.items?.get(id)

    fun CollectionEntry.getProgressToNextLevel(amount: Long): Pair<Int, Float> {
        val nextTier = tiers.entries.find { amount < it.value } ?: return maxTiers to 1.0f
        val nextAmount = nextTier.value
        val currentAmount = tiers.entries.find { amount >= it.value }?.value ?: 0
        val progress = (amount - currentAmount).toFloat() / (nextAmount - currentAmount)
        return nextTier.key.toInt() to progress
    }

    init {
        runBlocking {
            val collections = get()?.getAsJsonObject("collections") ?: return@runBlocking
            collectionData = collections.entrySet().associate { (key, value) ->
                key to value.asJsonObject.toCollectionCategory()
            }
        }
    }

    private fun JsonObject.toCollectionCategory() = CollectionCategory(
        this.getAsJsonObject("items").entrySet().associate { (key, value) ->
            key to value.asJsonObject.toCollectionEntry()
        },
    )

    private fun JsonObject.toCollectionEntry() = CollectionEntry(
        name = this["name"].asString(""),
        maxTiers = this["maxTiers"].asInt(0),
        tiers = this.getAsJsonArray("tiers").associate {
            it.asJsonObject.let {
                it["tier"].asString("") to it["amountRequired"].asLong(0)
            }
        },
    )


    private suspend fun get(): JsonObject? = Http.getResult<JsonObject>(url = API_URL).getOrNull()
}
