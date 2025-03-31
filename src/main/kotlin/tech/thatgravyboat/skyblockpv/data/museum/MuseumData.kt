package tech.thatgravyboat.skyblockpv.data.museum

import com.google.gson.JsonObject
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.Utils.toDashlessString
import tech.thatgravyboat.skyblockpv.utils.getNbt
import tech.thatgravyboat.skyblockpv.utils.legacyStack

data class MuseumData(val items: List<List<Lazy<ItemStack>>>) {
    companion object {
        fun fromJson(profile: SkyBlockProfile, members: JsonObject?): MuseumData? {
            members ?: return null
            val member = (members[profile.userId.toDashlessString()].takeIf { it is JsonObject } ?: return null).asJsonObject
            val asJsonObject = member["items"].asJsonObject
            val map = asJsonObject.entrySet().map { it.value as JsonObject }.map {
                it["items"].asJsonObject["data"].getNbt().getListOrEmpty("i")
            }.map { it.map { item -> lazy { item.legacyStack() } } }
            return MuseumData(map)
        }
    }
}
