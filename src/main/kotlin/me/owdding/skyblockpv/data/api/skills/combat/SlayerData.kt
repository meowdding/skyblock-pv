package me.owdding.skyblockpv.data.api.skills.combat

import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.resources.ResourceLocation

fun getIconFromSlayerName(name: String): ResourceLocation = SkyBlockPv.id("icon/slayer/$name")

data class SlayerTypeData(
    val exp: Long,
    val bossAttemptsTier: Map<Int, Int>,
    val bossKillsTier: Map<Int, Int>,
)
