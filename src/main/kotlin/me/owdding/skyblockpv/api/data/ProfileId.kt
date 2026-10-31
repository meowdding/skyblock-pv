package me.owdding.skyblockpv.api.data

import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import java.util.*

data class ProfileId(
    val id: UUID,
    val name: String,
    val user: UUID,
) {
    // api can say the currently selected profile is a profile we already switched from this makes
    // sure the profile is the one we actually have selected, before posting the sbapi events
    fun ensureSelectedProfile(): Boolean {
        return user == McPlayer.uuid && ProfileAPI.isLoaded && name == ProfileAPI.profileName
    }
}
