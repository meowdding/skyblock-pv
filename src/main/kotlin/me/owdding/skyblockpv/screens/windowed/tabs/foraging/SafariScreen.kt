package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SafariCodecs
import me.owdding.skyblockpv.utils.components.PvLayouts
import net.minecraft.client.gui.layouts.Layout

class SafariScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseForagingScreen(gameProfile, profile) {
    override val type: ForagingCategory = ForagingCategory.SAFARI

    override fun getLayout(bg: DisplayWidget): Layout {
        println(profile.safari?.milestone)
        println(profile.safari?.tickets)
        println(profile.safari?.biomeCaptures)
        println(profile.safari?.discoveredCritters)

        return PvLayouts.horizontal(alignment = 0.5f) {
            string(profile.safari?.milestone.toString() + " levels: " + SafariCodecs.data.milestones)
            string(profile.safari?.tickets.toString())
            string(profile.safari?.biomeCaptures.toString())
            string("")
            string(profile.safari?.discoveredCritters.toString())
            string("out of")
            string(SafariCodecs.data.critters.toString())
        }
    }
}
