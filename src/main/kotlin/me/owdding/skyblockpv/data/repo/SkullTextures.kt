package me.owdding.skyblockpv.data.repo

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.skyblockpv.SkyBlockPv

enum class SkullTextures {
    BACKPACK,
    ACCESSORY_BAG,
    SACKS,
    PERSONAL_VAULT,
    DUNGEONS,
    MORT,
    HOTM,
    HOTF,
    CHOCOLATE_FACTORY,
    COACH_JACKRABBIT,
    RIFT,
    ;

    val skull by lazy { createSkull(textures[name.lowercase()].orEmpty()) }

    @Module
    companion object {
        private val textures: Map<String, String> by SkyBlockPv.repoDelegate("pv/skull_textures", emptyMap())
    }
}
