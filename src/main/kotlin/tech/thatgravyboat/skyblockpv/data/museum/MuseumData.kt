package tech.thatgravyboat.skyblockpv.data.museum

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.LegacyItemStack
import tech.thatgravyboat.skyblockpv.utils.Utils.toDashlessString
import tech.thatgravyboat.skyblockpv.utils.getNbt
import tech.thatgravyboat.skyblockpv.utils.getPath

data class MuseumData(val items: List<MuseumEntry>) {
    companion object {
        fun fromJson(profile: SkyBlockProfile, members: JsonObject?): MuseumData? {
            val asJsonObject = members?.getPath("${profile.userId.toDashlessString()}.items")
                ?.let { if (it is JsonObject) it else null } ?: return null
            val map = asJsonObject.entrySet().map { it.key to it.value as JsonObject }.mapNotNull {
                it.second.getPath("items.data")?.getNbt()?.getListOrEmpty("i")?.let { value -> it.first to value }
            }.map { MuseumEntry(it.first, it.second.map { item -> LegacyItemStack.fromTag(item) }) }
            return MuseumData(map)
        }
    }
}

data class MuseumEntry(val id: String, val stacks: List<LegacyItemStack>)
