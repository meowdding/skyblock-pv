package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.auctions.AuctionData
import java.util.*

private const val PATH = "/auctions"

object AuctionAPI: CachedApi<SkyBlockProfile, AuctionData, UUID>() {
    override fun path(data: SkyBlockProfile) = "$PATH/${data.id.id}"
    override fun decode(data: JsonObject, profile: SkyBlockProfile) = AuctionData.fromJson(data.get("auctions").asJsonArray, profile)
    override fun getKey(data: SkyBlockProfile) = data.id.id
}
