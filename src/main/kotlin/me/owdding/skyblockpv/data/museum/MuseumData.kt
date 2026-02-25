package me.owdding.skyblockpv.data.museum

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils.toDashlessString
import me.owdding.skyblockpv.utils.getNbt
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.json.getPathAs
import me.owdding.skyblockpv.utils.legacyStack
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.remote.MuseumEntry
import tech.thatgravyboat.skyblockapi.api.events.remote.SkyBlockPvMuseumOpenedEvent
import tech.thatgravyboat.skyblockapi.api.events.remote.SkyBlockPvRequired
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class MuseumData(val items: List<MuseumEntry>, val special: List<Lazy<ItemStack>>) {
    companion object {
        @OptIn(SkyBlockPvRequired::class)
        fun fromJson(profile: SkyBlockProfile, members: JsonObject?): MuseumData {
            val asJsonObject = members?.getPathAs<JsonObject>(profile.userId.toDashlessString()) ?: return MuseumData(emptyList(), emptyList())

            val encodedItems = asJsonObject.getAs<JsonObject>("items")?.entrySet() ?: emptySet()
            val encodedSpecial = asJsonObject.getAs<JsonArray>("special")?.toList() ?: emptyList()

            fun JsonObject.decode() = getPath("items.data")?.getNbt()?.getListOrEmpty("i")?.map { item -> lazy { item.legacyStack() } }

            val items = encodedItems.map { it.key to it.value as JsonObject }.mapNotNull {
                val items = it.second.decode()

                TempMuseumEntry(it.first, items ?: return@mapNotNull null, it.second.getPathAs<Boolean>("borrowing", false))
            }

            val special = encodedSpecial.filterIsInstance<JsonObject>().mapNotNull { it.decode() }.flatten()
            if (profile.isOwnProfile) McClient.runNextTick {
                SkyBlockPvMuseumOpenedEvent(items.complete(false)).post(SkyBlockAPI.eventBus)
            }

            return MuseumData(items.complete(), special)
        }

        private fun List<TempMuseumEntry>.complete(includeBorrowing: Boolean = true) =
            this.map { entry -> entry.id to (entry.items.takeUnless { entry.borrowing && !includeBorrowing } ?: emptyList()) }
                .map { (id, items) -> MuseumEntry(id, items) }
    }

    fun isParentDonated(entry: MuseumRepoEntry, checkedIds: MutableSet<String> = mutableSetOf()): String? {
        val parentId = entry.parentId
        if (items.any { it.id == parentId }) {
            return parentId
        } else if (parentId != null && !checkedIds.contains(parentId)) {
            checkedIds.add(parentId)
            return RepoMuseumData.getById(parentId)?.let { isParentDonated(it, checkedIds) }
        }

        return null
    }

    private data class TempMuseumEntry(
        val id: String,
        val items: List<Lazy<ItemStack>>,
        val borrowing: Boolean,
    )
}
