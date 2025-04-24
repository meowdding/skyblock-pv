package me.owdding.skyblockpv.api

import me.owdding.skyblockpv.api.data.PlayerStatus
import me.owdding.skyblockpv.utils.ChatUtils
import java.util.*

private const val PATH = "/status"

object StatusAPI {
    suspend fun getStatus(uuid: UUID): PlayerStatus? {
        val result = HypixelAPI.get("$PATH/$uuid") ?: run {
            ChatUtils.chat("Something went wrong fetching the status from Hypixel. Report this on the Discord!")
            return null
        }
        return PlayerStatus.fromJson(result)
    }
}
