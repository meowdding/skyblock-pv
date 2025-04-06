package tech.thatgravyboat.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget

class BestiaryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        return LinearLayout.vertical()
    }
}

