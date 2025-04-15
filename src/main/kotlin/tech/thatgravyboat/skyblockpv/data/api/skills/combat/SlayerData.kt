package tech.thatgravyboat.skyblockpv.data.api.skills.combat

import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockpv.SkyBlockPv

fun getIconFromSlayerName(name: String): ResourceLocation = SkyBlockPv.id("icon/slayer/$name")

data class SlayerTypeData(
    val exp: Long,
    val bossAttemptsTier: Map<Int, Int>,
    val bossKillsTier: Map<Int, Int>,
)
