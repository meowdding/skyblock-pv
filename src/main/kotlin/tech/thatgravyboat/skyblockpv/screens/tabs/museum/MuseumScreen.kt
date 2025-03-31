package tech.thatgravyboat.skyblockpv.screens.tabs.museum

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild

class MuseumScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null): BaseMuseumScreen(gameProfile, profile) {

    override fun getLayout() = LayoutBuild.frame {
        string(loadingComponent { Text.of("meow :D") })
    }
}
