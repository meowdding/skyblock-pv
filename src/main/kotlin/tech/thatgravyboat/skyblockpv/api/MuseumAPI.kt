package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.museum.MuseumData
import java.util.*

private const val PATH = "v2/skyblock/museum"

object MuseumAPI: CachedApi<SkyBlockProfile, MuseumData, UUID>() {
    override fun path() = PATH
    override fun decode(data: JsonObject, originalData: SkyBlockProfile) = MuseumData.fromJson(originalData, data.getAsJsonObject("members"))
    override fun getKey(data: SkyBlockProfile) = data.id.id
    override fun variables(data: SkyBlockProfile) = mapOf("profile" to data.id.id.toString())
}
