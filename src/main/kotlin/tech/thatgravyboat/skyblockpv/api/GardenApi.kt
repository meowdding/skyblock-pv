package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.skills.farming.GardenProfile
import java.util.*

private const val PATH = "v2/skyblock/garden"

object GardenApi : CachedApi<SkyBlockProfile, GardenProfile, UUID>() {
    override fun path() = PATH
    override fun decode(data: JsonObject, originalData: SkyBlockProfile) = GardenProfile.fromJson(data.getAsJsonObject("garden"))
    override fun getKey(data: SkyBlockProfile) = data.id.id
    override fun variables(data: SkyBlockProfile) = mapOf("profile" to data.id.id.toString())
}
