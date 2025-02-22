package tech.thatgravyboat.skyblockpv.api

import tech.thatgravyboat.skyblockapi.utils.Logger
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockpv.api.data.PlayerStatus
import java.util.UUID

private const val PATH = "v2/status"

object StatusAPI {
    suspend fun getStatus(uuid: UUID): PlayerStatus? {
        val result = HypixelAPI.get(PATH, mapOf("uuid" to uuid.toString())) ?: run {
            Text.of("Something went wrong :3").send()
            return null
        }
        return PlayerStatus.fromJson(result)
    }
}
