package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import me.owdding.skyblockpv.data.api.CollectionCategory
import me.owdding.skyblockpv.data.api.CollectionEntry
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.http.Http

private const val API_URL = "https://api.hypixel.net/v2/resources/skyblock/collections"

@LoadData
object CollectionAPI : ExtraData {
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

    fun CollectionEntry.getProgressToMax(amount: Long): Float {
        val maxAmount = tiers.entries.maxOf { it.value }
        return (amount.toFloat() / maxAmount).coerceAtMost(1.0f)
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
    override fun load() {
        runBlocking {
            val collections = get()?.getAsJsonObject("collections") ?: return@runBlocking
            collectionData = collections.entrySet().associate { (key, value) ->
                key to value.asJsonObject.toCollectionCategory()
            }
        }
    }
}
