package me.owdding.skyblockpv.api.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.json.getPathAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase

data class HypixelPlayer(
    val socials: Map<Socials, String>,
) {
    companion object {
        data class SocialEntry(
            val name: String,
            val url: String,
            val shouldCopy: Boolean = false,
        )

        fun fromJson(json: JsonObject): HypixelPlayer {
            val social = json.getPathAs<JsonElement>("player.socialMedia.links")?.asMap { k, v -> Socials.byName(k) to v.asString } ?: emptyMap()
            return HypixelPlayer(social)
        }
    }

    enum class Socials(val shouldCopy: Boolean = false) {
        YOUTUBE,
        DISCORD(true),
        TWITTER,
        HYPIXEL,
        TWITCH,
        TIKTOK,
        INSTAGRAM,
        UNKNOWN;

        fun toEntry(url: String) = SocialEntry(this.name.toTitleCase(), url, shouldCopy)

        companion object {
            fun byName(key: String): Socials = entries.find { it.name.equals(key, true) } ?: UNKNOWN
        }
    }
}
