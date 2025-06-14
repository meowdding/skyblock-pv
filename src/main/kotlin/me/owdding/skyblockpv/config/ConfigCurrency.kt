package me.owdding.skyblockpv.config

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.utils.http.Http

private const val URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json"

@LoadData
object CurrenciesAPI : ExtraData {

    private val conversions = mutableMapOf<String, Double>()

    override suspend fun load() {
        val data = Http.getResult<JsonObject>(URL).getOrNull() ?: return
        val conversions = data.getAsJsonObject("usd")
        for (entry in conversions.entrySet()) {
            val key = entry.key
            val value = entry.value.asDouble
            CurrenciesAPI.conversions[key] = value
        }
    }

    fun convert(currency: ConfigCurrency, usd: Double): Pair<ConfigCurrency, Double> {
        return conversions[currency.name.lowercase()]?.let { rate -> Pair(currency, usd * rate) } ?: Pair(ConfigCurrency.USD, usd)
    }
}

enum class ConfigCurrency {
    // Real Money
    USD,
    EUR,
    JPY,
    GBP,
    AUD,
    CAD,
    CHF,
    CNH,
    HKD,
    NZD,
    CZK,
    ZWL,

    // Fake Money
    BTC,
    ETH,
    DOGE,
}
