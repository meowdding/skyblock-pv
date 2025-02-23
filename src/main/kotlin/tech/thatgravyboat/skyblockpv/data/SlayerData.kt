package tech.thatgravyboat.skyblockpv.data

import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockpv.SkyBlockPv

private val xpPerLevelNormal = mapOf(
    1 to 10L,
    2 to 30L,
    3 to 250L,
    4 to 1_500L,
    5 to 5_000L,
    6 to 20_000L,
    7 to 100_000L,
    8 to 400_000L,
    9 to 1_000_000L,
)

private val xpPerLevelVampire = mapOf(
    1 to 20L,
    2 to 75L,
    3 to 240L,
    4 to 840L,
    5 to 2_400L,
)


fun getIconFromSlayerName(name: String): ResourceLocation = SkyBlockPv.id(
    when (name) {
        "zombie" -> "icon/slayer/revenant"
        "spider" -> "icon/slayer/tarantula"
        "wolf" -> "icon/slayer/sven"
        "enderman" -> "icon/slayer/voidgloom"
        "blaze" -> "icon/slayer/inferno"
        else -> "icon/questionmark"
    },
)

fun getSlayerLevel(slayer: String, xp: Long): Int {
    val xpPerLevel = when (slayer) {
        "vampire" -> xpPerLevelVampire
        else -> xpPerLevelNormal
    }

    return xpPerLevel.entries.lastOrNull { it.value < xp }?.key ?: 0
}

data class SlayerTypeData(
    val exp: Long,
    val bossAttemptsTier: Map<Int, Int>,
    val bossKillsTier: Map<Int, Int>,
)
