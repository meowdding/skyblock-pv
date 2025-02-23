package tech.thatgravyboat.skyblockpv.data

data class Currency(
    val purse: Long,
    val mainBank: Long,
    val soloBank: Long = 0,
    val motes: Long,
    val cookieBuffActive: Boolean,
)
