package me.owdding.skyblockpv.data.repo

import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData

enum class SkullTextures(var texture: String = "") {
    BACKPACK,
    ACCESSORY_BAG,
    SACKS,
    PERSONAL_VAULT,
    DUNGEONS,
    HOTM,
    HOTF,
    CHOCOLATE_FACTORY,
    COACH_JACKRABBIT,
    RIFT,
    ;

    val skull by lazy { createSkull(texture) }

    @LoadData
    companion object : DefaultedData {
        override suspend fun load() {
            val textures = Utils.loadFromRemoteRepo<Map<String, String>>("pv/skull_textures") ?: emptyMap()
            textures.entries.forEach { (key, texture) -> valueOf(key.uppercase()).texture = texture }
        }
    }
}
