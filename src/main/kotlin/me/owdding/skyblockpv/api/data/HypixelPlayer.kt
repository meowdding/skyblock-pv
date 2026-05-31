package me.owdding.skyblockpv.api.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.json.getPathAs
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

data class HypixelPlayer(
    val socials: Map<Social, String>,
    val rank: Rank,
    private val rawPrefix: String? = null,
) {
    val prefix: Component = if (rawPrefix != null) {
        Text.of(rawPrefix)
    } else {
        val tag = rank.displayName
        Text.of("[$tag]", rank.color)
    }

    companion object {
        fun fromJson(json: JsonObject): HypixelPlayer {
            val social = json.getPathAs<JsonElement>("player.socialMedia.links")?.asMap { k, v ->
                Social.byName(k) to v.asString
            } ?: emptyMap()

            val prefixString = json.getPathAs<String>("player.prefix")?.takeUnless { it.isBlank() }

            val rank = run {
                if (prefixString != null) {
                    Rank.byName(prefixString)
                } else {
                    fun takeOrNull(path: String): Rank.Standard? {
                        val value = json.getPathAs<String>("player.$path")
                        if (value.isNullOrBlank() || value == "NONE" || value == "NORMAL") return null
                        return Rank.Standard.entries.find { it.name.equals(value, true) }
                    }

                    takeOrNull("rank")
                        ?: takeOrNull("monthlyPackageRank")
                        ?: takeOrNull("newPackageRank")
                        ?: takeOrNull("packageRank")
                        ?: Rank.Standard.NONE
                }
            }

            return HypixelPlayer(social, rank, prefixString)
        }
    }
}

sealed interface Rank {
    val color: Int
    val displayName: String

    enum class Standard(override val color: Int, displayName: String? = null) : Rank {
        NONE(TextColor.GRAY),
        VIP(TextColor.GREEN),
        VIP_PLUS(TextColor.GREEN, "VIP+"),
        MVP(TextColor.AQUA),
        MVP_PLUS(TextColor.AQUA, "MVP+"),
        SUPERSTAR(TextColor.ORANGE, "MVP++"),
        YOUTUBER(TextColor.RED, "YOUTUBE"),
        PIG_PLUS_PLUS_PLUS(TextColor.PINK, "PIG+++"),
        STAFF(TextColor.RED, "ዞ");

        override val displayName: String = displayName ?: name
    }

    data class Unknown(val key: String) : Rank {
        override val color: Int = TextColor.DARK_PURPLE
        override val displayName: String = key
    }

    companion object {
        fun byName(key: String): Rank {
            val cleanKey = key.stripColor().replace("[", "").replace("]", "")

            return Standard.entries.find {
                it.name.equals(cleanKey, true) || it.displayName.equals(cleanKey, true)
            } ?: Unknown(cleanKey)
        }
    }
}

enum class Social(val shouldCopy: Boolean = false) {
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
        fun byName(key: String): Social = entries.find { it.name.equals(key, true) } ?: UNKNOWN
    }
}

data class SocialEntry(
    val name: String,
    val url: String,
    val shouldCopy: Boolean = false,
)
