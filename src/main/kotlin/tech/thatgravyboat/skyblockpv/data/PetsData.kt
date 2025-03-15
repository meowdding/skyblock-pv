package tech.thatgravyboat.skyblockpv.data

data class Pet(
    val uuid: String?,
    val uniqueId: String?, // TODO: whats the difference? why can one be null? what the fuck hypixel
    val type: String,
    val exp: Long,
    val active: Boolean,
    val tier: String,
    val heldItem: String?,
    val candyUsed: Int,
    val skin: String?,
)
