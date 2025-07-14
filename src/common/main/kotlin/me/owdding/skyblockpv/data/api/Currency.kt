package me.owdding.skyblockpv.data.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.data.SortedEntry.Companion.sortToEssenceOrder
import me.owdding.skyblockpv.utils.json.getPathAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asList
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class Currency(
    val purse: Long,
    val motes: Long,
    val cookieBuffActive: Boolean,
    val essence: Map<String, Long>,
) {
    companion object {
        fun fromJson(member: JsonObject): Currency {
            val currency = member.getPathAs<JsonObject>("currencies") ?: JsonObject()
            return Currency(
                purse = currency["coin_purse"].asLong(0),
                motes = currency["motes_purse"].asLong(0),
                cookieBuffActive = member.getPathAs<Boolean>("profile.cookie_buff_active", false),
                // todo: add missing essences if not unlocked
                essence = currency["essence"].asMap { id, obj -> id to obj.asJsonObject["current"].asLong(0) }.sortToEssenceOrder(),
            )
        }
    }
}

data class Bank(
    val profileBank: Long,
    val soloBank: Long,
    val history: List<Transaction>,
) {
    companion object {
        fun fromJson(json: JsonObject, member: JsonObject): Bank? {
            if (!json.has("banking")) return null
            return Bank(
                profileBank = json.getPath("banking.balance").asLong(0),
                soloBank = member.getPath("profile.bank_account").asLong(0),
                history = json.getPath("banking.transactions").asList { Transaction.fromJson(it.asJsonObject) }.sortedByDescending { it.timestamp }.take(7),
            )
        }
    }
}

data class Transaction(
    val amount: Long,
    val timestamp: Long,
    val action: String,
    val initiator: String,
) {
    companion object {
        fun fromJson(json: JsonObject) = Transaction(
            amount = json["amount"].asLong(0),
            timestamp = json["timestamp"].asLong(0),
            action = json["action"].asString(""),
            initiator = json["initiator_name"].asString(""),
        )
    }
}
