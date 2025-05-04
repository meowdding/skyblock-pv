package me.owdding.skyblockpv.data.auctions

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils.toDashlessString
import me.owdding.skyblockpv.utils.apiItemStacks
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.asBoolean
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.extentions.asUUID
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.util.UUID

data class AuctionData(
    val auctions: List<AuctionEntry>,
) {

    companion object {

        fun fromJson(auctions: JsonArray, profile: SkyBlockProfile): AuctionData {
            return AuctionData(
                auctions.map { element ->
                    val json = element as JsonObject
                    val start = json.get("start").asLong(0)
                    val end = json.get("end").asLong(0)
                    val bid = json.get("highest_bid_amount").asLong(0)
                    val startingBid = json.get("starting_bid").asLong(0)
                    val bin = json.get("bin").asBoolean(false)
                    val uuid = json.get("uuid").asString()
                    val item = json.getPath("item_bytes.data")?.apiItemStacks()?.firstOrNull() ?: ItemStack.EMPTY
                    val auctioneer = if (json.get("profile_id").asString() == json.get("auctioneer").asString()) {
                        profile.userId.toDashlessString()
                    } else {
                        json.get("auctioneer").asString()
                    }

                    AuctionEntry(
                        id = uuid,
                        start = start,
                        end = end,
                        item = item,
                        bid = bid,
                        startingBid = startingBid,
                        bin = bin,
                        auctioneer = auctioneer,
                    )
                }
            )
        }
    }
}

data class AuctionEntry(
    val id: String?,
    val bin: Boolean,

    val start: Long,
    val end: Long,

    val item: ItemStack,

    val bid: Long,
    val startingBid: Long,

    val auctioneer: String? = null,
)
