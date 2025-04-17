package tech.thatgravyboat.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget

class CrimsonIsleScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val profile = profile?: return LayoutBuild.frame {  }
        return LayoutBuild.vertical {
            string(profile.crimsonIsleData.selectedFaction?.id?: "none")
        }
    }
}
