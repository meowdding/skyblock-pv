package tech.thatgravyboat.skyblockpv.api

import tech.thatgravyboat.skyblockpv.api.data.PlayerStatus
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import java.util.*

private const val PATH = "v2/status"

object StatusAPI {
    suspend fun getStatus(uuid: UUID): PlayerStatus? {
        val result = HypixelAPI.get(PATH, mapOf("uuid" to uuid.toString())) ?: run {
            ChatUtils.chat("Something went wrong :3")
            return null
        }
        return PlayerStatus.fromJson(result)
    }
}
