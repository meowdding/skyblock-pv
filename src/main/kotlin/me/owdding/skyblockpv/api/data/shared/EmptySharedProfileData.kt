package me.owdding.skyblockpv.api.data.shared

import java.util.*

data class EmptySharedProfileData(
    override val id: UUID,
    val reason: Reason,
    val throwable: Throwable? = null,
) : SharedProfileData {
    override val backingProfile: BackingSharedProfile = BackingSharedProfile(
        UUID.randomUUID()
    )
    override val isEmpty: Boolean = true

    enum class Reason {
        LOADING,
        NO_PROFILES,
        NOT_SHARED,
        ERROR,
        ;
    }
}
