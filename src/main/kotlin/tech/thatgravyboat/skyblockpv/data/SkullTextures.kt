package tech.thatgravyboat.skyblockpv.data

import tech.thatgravyboat.skyblockpv.utils.Utils

enum class SkullTextures(var texture: String = "") {
    WITHER_ESSENCE,
    SPIDER_ESSENCE,
    UNDEAD_ESSENCE,
    DRAGON_ESSENCE,
    GOLD_ESSENCE,
    DIAMOND_ESSENCE,
    ICE_ESSENCE,
    CRIMSON_ESSENCE,
    BACKPACK,
    ACCESSORY_BAG,
    PERSONAL_VAULT,
    DUNGEONS,
    HOTM,
    CF,
    RABBIT_BRO,
    RABBIT_COUSIN,
    RABBIT_SIS,
    RABBIT_DADDY,
    RABBIT_GRANNY,
    RABBIT_UNCLE,
    RABBIT_DOG,
    COACH_JACKRABBIT,
    ;

    fun createSkull() = tech.thatgravyboat.skyblockpv.utils.createSkull(texture)

    companion object {
        init {
            val textures = Utils.loadFromRepo<Map<String, String>>("skull_textures") ?: emptyMap()
            textures.entries.forEach { (key, texture) -> valueOf(key.uppercase()).texture = texture }
        }
    }
}
