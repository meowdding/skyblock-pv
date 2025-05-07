package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import java.util.*
import java.util.concurrent.CompletableFuture

private const val PATH = "/profiles"

object ProfileAPI : CachedApi<UUID, List<SkyBlockProfile>, UUID>() {

    override fun path(data: UUID) = "$PATH/$data"

    override fun decode(data: JsonObject, originalData: UUID): List<SkyBlockProfile> {
        val startTime = System.currentTimeMillis()

        val profiles = data.getAsJsonArray("profiles").map {
            CompletableFuture.supplyAsync {
                val profile = SkyBlockProfile.fromJson(it.asJsonObject, originalData)
                profile
            }
        }

        CompletableFuture.allOf(*profiles.toTypedArray()).join()

        val diff = System.currentTimeMillis() - startTime
        if (SkyBlockPv.isDevMode) SkyBlockPv.info("Finished parsing after ${diff.toFormattedString()}ms")

        return profiles.mapNotNull { future -> future.resultNow() }
    }
    override fun getKey(data: UUID) = data

    suspend fun getProfiles(uuid: UUID): List<SkyBlockProfile> = getData(uuid).getOrElse {
        SkyBlockPv.error("Failed to get profiles for $uuid", it)
        emptyList()
    }
}
