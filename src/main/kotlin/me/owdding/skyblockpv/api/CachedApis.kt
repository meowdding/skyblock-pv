package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.HypixelPlayer
import me.owdding.skyblockpv.api.data.PlayerStatus
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.farming.GardenProfile
import me.owdding.skyblockpv.data.museum.MuseumData
import me.owdding.skyblockpv.utils.Utils.mapInParallel
import java.util.*

object CachedApis {

    fun clearCaches() {
        ProfileAPI.clearCache()
        StatusAPI.clearCache()
        GardenAPI.clearCache()
        MuseumAPI.clearCache()
        PlayerAPI.clearCache()
    }
}

object GardenAPI : CachedApi<SkyBlockProfile, GardenProfile, UUID>() {
    override fun path(data: SkyBlockProfile) = "/garden/${data.id.id}"
    override fun decode(data: JsonObject, originalData: SkyBlockProfile) = GardenProfile.fromJson(data.getAsJsonObject("garden"))
    override fun getKey(data: SkyBlockProfile) = data.id.id
}

object MuseumAPI : CachedApi<SkyBlockProfile, MuseumData, UUID>() {
    override fun path(data: SkyBlockProfile) = "/museum/${data.id.id}"
    override fun decode(data: JsonObject, originalData: SkyBlockProfile) = MuseumData.fromJson(originalData, data.getAsJsonObject("members"))
    override fun getKey(data: SkyBlockProfile) = data.id.id
}

object StatusAPI : CachedApi<UUID, PlayerStatus, UUID>() {
    override fun path(data: UUID) = "/status/$data"
    override fun decode(data: JsonObject, originalData: UUID) = PlayerStatus.fromJson(data)
    override fun getKey(data: UUID) = data
}

object ProfileAPI : CachedApi<UUID, List<SkyBlockProfile>, UUID>() {

    override fun path(data: UUID) = "/profiles/$data"
    override fun decode(data: JsonObject, originalData: UUID) = data.getAsJsonArray("profiles")
        .mapInParallel { element -> SkyBlockProfile.fromJson(element.asJsonObject, originalData) }
        .filterNotNull()

    override fun getKey(data: UUID) = data

    fun getProfiles(gameProfile: GameProfile, intent: String, handler: (List<SkyBlockProfile>) -> Unit) = getDataAsync(gameProfile.id, intent) { result ->
        result
            .onSuccess(handler)
            .onFailure {
                SkyBlockPv.error("Failed to get profiles for: ${gameProfile.name} (${gameProfile.id})", it)
                handler(listOf())
            }
    }
}

object PlayerAPI : CachedApi<UUID, HypixelPlayer, UUID>(Long.MAX_VALUE) {
    override fun path(data: UUID) = "/player/$data"
    override fun decode(data: JsonObject, originalData: UUID): HypixelPlayer = HypixelPlayer.fromJson(data)
    override fun getKey(data: UUID) = data

    fun getPlayer(gameProfile: GameProfile, handler: (HypixelPlayer?) -> Unit) = getDataAsync(gameProfile.id, "cache") { result ->
        result
            .onSuccess(handler)
            .onFailure {
                SkyBlockPv.error("Failed to get player for: ${gameProfile.name} (${gameProfile.id})", it)
                handler(null)
            }
    }
}
