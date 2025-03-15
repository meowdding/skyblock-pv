package tech.thatgravyboat.skyblockpv.data

import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson

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
    ;

    fun createSkull() = tech.thatgravyboat.skyblockpv.utils.createSkull(texture)

    companion object {
        init {
            // todo: utils method to load this stuff
            runBlocking {
                try {
                    val textures = this.javaClass.getResourceAsStream("/repo/skull_textures.json")?.readJson<Map<String, String>>() ?: return@runBlocking
                    textures.entries.forEach { (key, texture) -> valueOf(key.uppercase()).texture = texture }
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }
}
