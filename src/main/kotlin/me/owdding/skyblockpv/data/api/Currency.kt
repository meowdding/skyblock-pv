package me.owdding.skyblockpv.data.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.data.SortedEntry.Companion.sortToEssenceOrder
import me.owdding.skyblockpv.utils.ParseHelper
import me.owdding.skyblockpv.utils.json.getPathAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asList
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class Currency(override val json: JsonObject) : ParseHelper {
    val purse: Long by long("currencies.coin_purse")
    val motes: Long by long("currencies.motes_purse")
    val cookieBuffActive: Boolean by boolean("profile.cookie_buff_active")
    val essence: Map<String, Long> by map("currencies.essence") { id, obj -> id to obj.asJsonObject["current"].asLong(0) }.map { it.sortToEssenceOrder() }
}

data class Bank(override val json: JsonObject, val member: JsonObject) : ParseHelper {
    val profileBank: Long by long("banking.balance")
    val soloBank: Long = member.getPath("profile.bank_account").asLong(0)
    val history: List<Transaction> by list("banking.transactions") { Transaction(it.asJsonObject) }.map { it.sortedByDescending { it.timestamp }.take(7) }
}

data class Transaction(override val json: JsonObject) : ParseHelper {
    val amount: Long by long()
    val timestamp: Long by long()
    val action: String by string()
    val initiator: String by string("initiator_name")
}
