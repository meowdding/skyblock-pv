package me.owdding.skyblockpv.data.museum

import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils.toDashlessString
import me.owdding.skyblockpv.utils.getNbt
import me.owdding.skyblockpv.utils.json.getPathAs
import me.owdding.skyblockpv.utils.legacyStack
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.remote.MuseumEntry
import tech.thatgravyboat.skyblockapi.api.events.remote.SkyBlockPvMuseumOpenedEvent
import tech.thatgravyboat.skyblockapi.api.events.remote.SkyBlockPvRequired
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class MuseumData(val items: List<MuseumEntry>) {
    companion object {
        @OptIn(SkyBlockPvRequired::class)
        fun fromJson(profile: SkyBlockProfile, members: JsonObject?): MuseumData {
            val asJsonObject = members?.getPath("${profile.userId.toDashlessString()}.items")
                ?.let { it as? JsonObject } ?: return MuseumData(emptyList())
            val map = asJsonObject.entrySet().map { it.key to it.value as JsonObject }.mapNotNull {
                val items = it.second.getPath("items.data")?.getNbt()?.getListOrEmpty("i") ?: return@mapNotNull null

                TempMuseumEntry(it.first, items.map { item -> lazy { item.legacyStack() } }, it.second.getPathAs<Boolean>("borrowing", false))
            }

            if (profile.isOwnProfile) SkyBlockPvMuseumOpenedEvent(map.complete(false)).post(SkyBlockAPI.eventBus)

            return MuseumData(map.complete())
        }

        private fun List<TempMuseumEntry>.complete(includeBorrowing: Boolean = true) =
            this.map { entry -> entry.id to (entry.items.takeUnless { entry.borrowing && !includeBorrowing } ?: emptyList()) }
                .map { (id, items) -> MuseumEntry(id, items) }
    }

    fun isParentDonated(entry: MuseumRepoEntry): String? {
        val parentId = entry.parentId
        if (items.any { it.id == parentId }) {
            return parentId
        } else if (parentId != null) {
            return RepoMuseumData.getById(parentId)?.let { isParentDonated(it) }
        }

        return null
    }

    private data class TempMuseumEntry(
        val id: String,
        val items: List<Lazy<ItemStack>>,
        val borrowing: Boolean,
    )
}
