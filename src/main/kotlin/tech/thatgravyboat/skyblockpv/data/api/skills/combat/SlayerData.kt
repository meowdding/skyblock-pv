package tech.thatgravyboat.skyblockpv.data.api.skills.combat

import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockpv.SkyBlockPv

fun getIconFromSlayerName(name: String): ResourceLocation = SkyBlockPv.id(
    when (name) {
        "revenant" -> "icon/slayer/revenant"
        "tarantula" -> "icon/slayer/tarantula"
        "sven" -> "icon/slayer/sven"
        "voidgloom" -> "icon/slayer/voidgloom"
        "inferno_demonlord" -> "icon/slayer/inferno"
        "vampire" -> "icon/slayer/vampire"
        else -> "icon/questionmark"
    },
)

data class SlayerTypeData(
    val exp: Long,
    val bossAttemptsTier: Map<Int, Int>,
    val bossKillsTier: Map<Int, Int>,
)
