package me.owdding.skyblockpv.api.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.json.getPathAs
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
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
        val tag = rank.displayName ?: rank.name
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
                    fun takeOrNull(path: String) = json.getPathAs<String>("player.$path")?.takeUnless { it in listOf("NONE", "NORMAL") }
                    val rankId = takeOrNull("rank")
                        ?: takeOrNull("monthlyPackageRank")
                        ?: takeOrNull("newPackageRank")
                        ?: takeOrNull("packageRank")

                    rankId?.let { Rank.byName(it) } ?: Rank.NONE
                }
            }

            println("---------")
            println("Prefix: $prefixString")
            println("Rank: $rank")
            println("---------")

            return HypixelPlayer(social, rank, prefixString)
        }
    }
}

enum class Rank(val color: Int, displayName: String? = null) {
    NONE(TextColor.GRAY),
    VIP(TextColor.GREEN),
    VIP_PLUS(TextColor.GREEN, "VIP+"),
    MVP(TextColor.AQUA),
    MVP_PLUS(TextColor.AQUA, "MVP+"),
    SUPERSTAR(TextColor.ORANGE, "MVP++"),
    YOUTUBER(TextColor.RED, "YOUTUBE"),
    PIG_PLUS_PLUS_PLUS(TextColor.PINK, "PIG+++"),
    STAFF(TextColor.RED, "ዞ"),
    ;

    val displayName: String = displayName ?: name

    companion object {
        fun byName(key: String): Rank {
            val cleanKey = key.stripColor().replace("[", "").replace("]", "")
            return entries.find { it.name.equals(cleanKey, true) || it.displayName.equals(cleanKey, true) } ?: run {
                println("Unknown rank: $key")
                NONE
            }
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
