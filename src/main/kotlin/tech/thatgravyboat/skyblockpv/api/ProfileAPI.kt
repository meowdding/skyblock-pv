package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import java.util.*

private const val PATH = "v2/skyblock/profiles"

object ProfileAPI : CachedApi<UUID, List<SkyBlockProfile>, UUID>() {

    override fun path() = PATH
    override fun decode(data: JsonObject, originalData: UUID) = data.getAsJsonArray("profiles").mapNotNull {
        SkyBlockProfile.fromJson(it.asJsonObject, originalData)
    }
    override fun getKey(data: UUID) = data
    override fun variables(data: UUID) = mapOf("uuid" to data.toString())

    suspend fun getProfiles(uuid: UUID): List<SkyBlockProfile> = getData(uuid).getOrNull() ?: emptyList()
}
