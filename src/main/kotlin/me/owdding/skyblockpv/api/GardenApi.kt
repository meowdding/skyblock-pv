package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.farming.GardenProfile
import java.util.*

private const val PATH = "/garden"

object GardenApi : CachedApi<SkyBlockProfile, GardenProfile, UUID>() {
    override fun path(data: SkyBlockProfile) = "$PATH/${data.id.id}"
    override fun decode(data: JsonObject, originalData: SkyBlockProfile) = GardenProfile.fromJson(data.getAsJsonObject("garden"))
    override fun getKey(data: SkyBlockProfile) = data.id.id
}
