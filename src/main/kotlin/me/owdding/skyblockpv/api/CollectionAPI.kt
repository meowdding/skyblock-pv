package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.data.api.CollectionCategory
import me.owdding.skyblockpv.data.api.CollectionEntry
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.http.Http

private const val API_URL = "https://api.hypixel.net/v2/resources/skyblock/collections"

@Module
object CollectionAPI {
    var collectionData: Map<String, CollectionCategory> = emptyMap()
        private set

    fun getIconFromCollectionType(type: String) = when (type) {
        "MINING" -> Items.STONE_PICKAXE.defaultInstance
        "FARMING" -> Items.GOLDEN_HOE.defaultInstance
        "COMBAT" -> Items.STONE_SWORD.defaultInstance
        "FORAGING" -> Items.JUNGLE_SAPLING.defaultInstance
        "FISHING" -> Items.FISHING_ROD.defaultInstance
        "RIFT" -> Items.MYCELIUM.defaultInstance
        else -> Items.BARRIER.defaultInstance
    }

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
