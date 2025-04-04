package tech.thatgravyboat.skyblockpv.screens.tabs.museum

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.museum.RepoMuseumData

class WeaponMuseumScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractMuseumItemScreen(gameProfile, profile) {

    override fun getMuseumData() = listOf(RepoMuseumData.weapons, RepoMuseumData.rarities).flatten()
}
