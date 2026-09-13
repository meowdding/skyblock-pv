package me.owdding.skyblockpv.config

import com.google.gson.JsonObject
import com.ibm.icu.text.NumberFormat
import com.ibm.icu.util.Currency
import com.ibm.icu.util.ULocale
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData
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
    val isFakeMoney: Boolean = false,
) {
    // Real Money
    AUD(ULocale("en", "AU")),
    ARS("es", "AR"),
    BRL("pt", "BR"),
    CAD(ULocale.CANADA),
    CHF("de", "CH"),
    CNH("zh", "HK"),
    CZK("cs", "CZ"),
    CLP("es", "CL"),
    DKK("da", "DK"),
    EUR(ULocale.GERMANY),
    GBP(ULocale.UK),
    HKD("zh", "HK"),
    HUF("hu", "HU"),
    INR("hi", "IN"),
    IDR("id", "ID"),
    JPY(ULocale.JAPAN),
    KRW("ko", "KR"),
    MXN("es", "MX"),
    MYR("ms", "MY"),
    NZD("en", "NZ"),
    NOK("no", "NO"),
    PLN("pl", "PL"),
    PHP("en", "PH"),
    RUB("ru", "RU"),
    RON("ro", "RO"),
    SGD("en", "SG"),
    SEK("sv", "SE"),
    THB("th", "TH"),
    TRY("tr", "TR"),
    TWD("zh", "TW"),
    USD(ULocale.US),
    UAH("uk", "UA"),
    VND("vi", "VN"),
    ZAR("en", "ZA"),
    ZWG("en", "ZW"),

    // Fake Money
    BTC(ULocale.US, isFakeMoney = true),
    DOGE(ULocale.US, isFakeMoney = true),
    ETH(ULocale.US, isFakeMoney = true),
    LTC(ULocale.US, isFakeMoney = true),
    SOL(ULocale.US, isFakeMoney = true),
    ;

    constructor(language: String, country: String) : this(parse(language, country))

    val currencyType = if (isFakeMoney) null else runCatching {
        Currency.getInstance(name)
    }.onFailure {
        SkyBlockPv.warn("Failed to load currency for $name")
    }.getOrNull()

    fun format(number: Double): String {
        if (currencyType == null) {
            val formatted = if (number in -1.0..1.0 && number != 0.0) {
                String.format(uLocale.toLocale(), "%.8f", number).trimEnd('0').trimEnd('.', ',')
            } else {
                NumberFormat.getNumberInstance(uLocale).format(number)
            }
            return "$formatted $name"
        }

        val instance = NumberFormat.getCurrencyInstance(uLocale)
        instance.currency = currencyType
        return instance.format(number)
    }
}
