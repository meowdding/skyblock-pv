package me.owdding.skyblockpv.api.data.profile

import me.owdding.skyblockpv.api.data.ProfileId
import me.owdding.skyblockpv.feature.Networth
import net.minecraft.network.chat.Component
import java.util.*
import java.util.concurrent.CompletableFuture

data class EmptySkyBlockProfile(
    override val userId: UUID,
    val reason: Reason,
    val throwable: Throwable? = null,
) : SkyBlockProfile {
    override val backingProfile: BackingSkyBlockProfile = BackingSkyBlockProfile(
        true,
        ProfileId(UUID.randomUUID(), "Error"),
        userId,
    )
    override val netWorth: CompletableFuture<Networth> = CompletableFuture()
    override val magicalPower: Pair<Int, Component> = 0 to Component.empty()
    override val isEmpty: Boolean = true

    enum class Reason {
        LOADING,
        NO_PROFILES,
        ERROR,
        ;
    }
}
