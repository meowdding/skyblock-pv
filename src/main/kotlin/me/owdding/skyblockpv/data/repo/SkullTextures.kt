package me.owdding.skyblockpv.data.repo

import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
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
    companion object : ExtraData {
        override suspend fun load() {
            val textures = Utils.loadFromRepo<Map<String, String>>("skull_textures") ?: emptyMap()
            textures.entries.forEach { (key, texture) -> valueOf(key.uppercase()).texture = texture }
        }
    }
}
