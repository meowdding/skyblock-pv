package me.owdding.skyblockpv.data.museum

import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils.toDashlessString
import me.owdding.skyblockpv.utils.getNbt
import me.owdding.skyblockpv.utils.legacyStack
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class MuseumData(val items: List<MuseumEntry>) {
    companion object {
        fun fromJson(profile: SkyBlockProfile, members: JsonObject?): MuseumData? {
            val asJsonObject = members?.getPath("${profile.userId.toDashlessString()}.items")
                ?.let { it as? JsonObject } ?: return null
            val map = asJsonObject.entrySet().map { it.key to it.value as JsonObject }.mapNotNull {
                it.second.getPath("items.data")?.getNbt()?.getListOrEmpty("i")?.let { value -> it.first to value }
            }.map { MuseumEntry(it.first, it.second.map { item -> lazy { item.legacyStack() } }) }
            return MuseumData(map)
        }
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
}

data class MuseumEntry(
    val id: String,
    val stacks: List<Lazy<ItemStack>>,
)
