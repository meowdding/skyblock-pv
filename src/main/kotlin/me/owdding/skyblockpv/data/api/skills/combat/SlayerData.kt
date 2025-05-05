package me.owdding.skyblockpv.data.api.skills.combat

import com.google.gson.JsonObject
import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import kotlin.text.isDigit

fun getIconFromSlayerName(name: String): ResourceLocation = SkyBlockPv.id("icon/slayer/$name")

data class SlayerTypeData(
    val exp: Long,
    val bossAttemptsTier: Map<Int, Int>,
    val bossKillsTier: Map<Int, Int>,
) {
    companion object {
        val EMPTY = SlayerTypeData(0L, emptyMap(), emptyMap())

        fun fromJson(json: JsonObject) = SlayerTypeData(
            exp = json["xp"].asLong(0),
            bossAttemptsTier = json.entrySet().filter { it.key.startsWith("boss_attempts_tier_") }
                .associate { it.key.filter { it.isDigit() }.toInt() to it.value.asInt },
            bossKillsTier = json.entrySet().filter { it.key.startsWith("boss_kills_tier_") }
                .associate { it.key.filter { it.isDigit() }.toInt() to it.value.asInt },
        )
    }
}
