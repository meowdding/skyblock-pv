package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvLayouts
import net.minecraft.client.gui.layouts.Layout

class MainForagingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseForagingScreen(gameProfile, profile)  {
    override val type: ForagingCategory = ForagingCategory.MAIN

    override fun getLayout(bg: DisplayWidget): Layout = PvLayouts.frame {

    }
}
