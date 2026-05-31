package me.owdding.skyblockpv.config

import com.google.gson.JsonObject
import com.ibm.icu.text.NumberFormat
import com.ibm.icu.util.Currency
import com.ibm.icu.util.ULocale
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.http.Http

private const val URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json"

@LoadData
object CurrenciesAPI : DefaultedData {

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

private fun parse(language: String, country: String): ULocale {
    return runCatching {
        ULocale(language, country)
    }.onFailure {
        SkyBlockPv.warn("Unable to locate ULocale for $language-$country!")
    }.getOrDefault(ULocale.US)
}

enum class ConfigCurrency(
    val uLocale: ULocale,
) {
    // Real Money
    USD(ULocale.US),
    EUR(ULocale.GERMANY),
    JPY(ULocale.JAPAN),
    GBP(ULocale.UK),
    AUD(ULocale("en", "AU")),
    CAD(ULocale.CANADA),
    CHF("de", "CH"),
    CNH(ULocale.CHINA),
    HKD(ULocale.CHINA),
    NZD("en", "NZ"),
    CZK("cz", "CZ"),
    ZWL("en", "ZW"),
    INR("hi", "IN"),

    // Fake Money
    BTC(ULocale.US),
    ETH(ULocale.US),
    DOGE(ULocale.US),
    ;

    constructor(language: String, country: String) : this(parse(language, country))

    val currencyType = runCatching {
        Currency.getInstance(name)
    }.onFailure {
        if (this.name.length == 4) return@onFailure
        SkyBlockPv.warn("Failed to load currency for $name")
    }.getOrNull()

    fun format(number: Long): String {
        if (currencyType == null) return "${number.toFormattedString()} $name"

        val instance = NumberFormat.getCurrencyInstance(uLocale)
        instance.currency = currencyType
        return instance.format(number)
    }
}
