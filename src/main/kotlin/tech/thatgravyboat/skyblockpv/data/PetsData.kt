package tech.thatgravyboat.skyblockpv.data

import java.util.*

data class Pet(
    val uuid: UUID?,
    val uniqueId: UUID?, // TODO: whats the difference? why can one be null? what the fuck hypixel
    val type: String,
    val exp: Long,
    val active: Boolean,
    val tier: String,
    val heldItem: String?,
    val candyUsed: Int,
    val skin: String?,
)
