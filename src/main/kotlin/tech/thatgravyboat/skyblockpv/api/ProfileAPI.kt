package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import java.util.*
import java.util.concurrent.CompletableFuture

private const val PATH = "v2/skyblock/profiles"

object ProfileAPI : CachedApi<UUID, List<SkyBlockProfile>, UUID>() {

    override fun path() = PATH

    override fun decode(data: JsonObject, originalData: UUID): List<SkyBlockProfile> {
        val startTime = System.currentTimeMillis()
        SkyBlockPv.info("Started Parsing Profiles")

        val profiles = data.getAsJsonArray("profiles").map {
            CompletableFuture.supplyAsync {
                val profileStartTime = System.currentTimeMillis()
                val profile = SkyBlockProfile.fromJson(it.asJsonObject, originalData)
                SkyBlockPv.info("${profile?.id?.name} took ${(System.currentTimeMillis() - profileStartTime).toFormattedString()}ms")
                profile
            }
        }

        CompletableFuture.allOf(*profiles.toTypedArray()).join()

        val diff = System.currentTimeMillis() - startTime
        SkyBlockPv.info("Finished parsing after ${diff.toFormattedString()}ms")

        return profiles.mapNotNull { future -> future.resultNow() }
    }
    override fun getKey(data: UUID) = data
    override fun variables(data: UUID) = mapOf("uuid" to data.toString())

    suspend fun getProfiles(uuid: UUID): List<SkyBlockProfile> = getData(uuid).getOrNull() ?: emptyList()
}
