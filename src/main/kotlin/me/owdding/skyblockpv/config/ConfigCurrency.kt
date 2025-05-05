package me.owdding.skyblockpv.config

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.utils.http.Http

private const val URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json"

@Module
object CurrenciesAPI {

    private val conversions = mutableMapOf<String, Double>()

    init {
        runBlocking {
            val data = Http.getResult<JsonObject>(URL).getOrNull() ?: return@runBlocking
            val conversions = data.getAsJsonObject("usd")
            for (entry in conversions.entrySet()) {
                val key = entry.key
                val value = entry.value.asDouble
                CurrenciesAPI.conversions[key] = value
            }
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

    // Fake Money
    BTC,
    ETH,
    DOGE,
}
