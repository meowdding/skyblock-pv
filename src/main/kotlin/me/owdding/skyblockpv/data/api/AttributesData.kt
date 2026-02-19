package me.owdding.skyblockpv.data.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.profile.BackingSkyBlockProfile.Companion.future
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.json.getPathAs
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.associateByNotNull

data class Attribute(
    val id: String,
    val owned: Int,
    val capturedAt: Long,
    val syphoned: Int,
)

data class TrapData(
    val trapItem: String?,
    val mode: String?,
    val shard: String?,
    val location: String?,
    val captured: Boolean,
    val captureTime: Long,
    val placedAt: Long,
    val toolkitIndex: Int,
)

data class AttributesData(
    val data: List<Attribute>,
    val fusions: Int,
    val traps: List<TrapData>,
) {
    companion object {
        val EMPTY = AttributesData(emptyList(), 0, emptyList())

        fun fromJson(member: JsonObject) = future {
            val stacks = member.getPathAs<JsonObject>("attributes.stacks")?.asMap { key, element ->
                key to element.asInt(0)
            } ?: emptyMap()
            val owned = member.getPathAs<JsonArray>("shards.owned")
                ?.filterIsInstance<JsonObject>()
                ?.associateByNotNull {
                    it.getAs<String>("type")?.lowercase()
                }?.mapValues { (_, value) ->
                    value.getAs<Int>("amount_owned", 0) to value.getAs<Long>("captured", 0)
                } ?: emptyMap()

            owned.keys.filter { id ->
                RepoAPI.attributes().attributes().values.none { it.shardId.lowercase().removePrefix("shard_") == id }
            }.takeIf { it.isNotEmpty() }?.let { println(it.joinToString(", ", prefix = "[", postfix = "]")) }


            val traps = member.getPathAs<JsonArray>("shards.traps.active_traps")?.filterIsInstance<JsonObject>()?.map {
                TrapData(
                    trapItem = it.getAs<String>("trap_item"),
                    mode = it.getAs<String>("mode"),
                    location = it.getAs<String>("location"),
                    shard = it.getAs<String>("shard"),
                    placedAt = it.getAs<Long>("placed_at", -1),
                    captureTime = it.getAs<Long>("capture_time", -1),
                    captured = it.getAs<Boolean>("captured", false),
                    toolkitIndex = it.getAs<Int>("hunting_toolkit_index", -1),
                )
            } ?: emptyList()

            val defaultOwned = 0 to 0L
            val fusions = member.getPathAs<Int>("shards.fused", 0)

            val attributes = RepoAPI.attributes().attributes().values.mapNotNull {
                val id = it.id().lowercase()
                val ownedData = owned[it.shardId.lowercase().removePrefix("shard_")]
                val stacks = stacks[id]
                if (ownedData == null && stacks == null) {
                    return@mapNotNull null
                }

                val (owned, capturedAt) = ownedData ?: defaultOwned
                val syphoned = stacks ?: 0
                Attribute(id, owned, capturedAt, syphoned)
            }

            AttributesData(
                attributes,
                fusions,
                traps,
            )
        }
    }
}
