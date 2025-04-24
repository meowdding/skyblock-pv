package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.museum.MuseumData
import java.util.*

private const val PATH = "/museum"

object MuseumAPI: CachedApi<SkyBlockProfile, MuseumData, UUID>() {
    override fun path(data: SkyBlockProfile) = "$PATH/${data.id.id}"
    override fun decode(data: JsonObject, originalData: SkyBlockProfile) = MuseumData.fromJson(originalData, data.getAsJsonObject("members"))
    override fun getKey(data: SkyBlockProfile) = data.id.id
}
