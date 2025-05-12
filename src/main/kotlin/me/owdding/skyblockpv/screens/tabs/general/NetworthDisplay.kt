package me.owdding.skyblockpv.screens.tabs.general

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.shorten
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.config.CurrenciesAPI
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import tech.thatgravyboat.skyblockapi.api.remote.pricing.BazaarAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.roundToInt

object NetworthDisplay {

    private fun Display.addTooltip(networth: Pair<Long, Map<String, Long>>): Display {
        val cookiePrice = BazaarAPI.getProduct("BOOSTER_COOKIE")?.buyPrice ?: 0.0
        val networthCookies = if (cookiePrice > 0) (networth.first / cookiePrice).roundToInt() else 0
        val networthUSD = ((networthCookies * 325.0) / 675.0) * 4.99

        val (currency, networthConverted) = CurrenciesAPI.convert(Config.currency, networthUSD)

        if (cookiePrice <= 0) return this

        return this.withTooltip {
            this.add {
                this.append("Networth: ") { this.color = TextColor.YELLOW }
                this.append(networth.first.toFormattedString()) { this.color = TextColor.GREEN }
            }

            this.add {
                this.append("Net worth in Cookies: ") { this.color = TextColor.YELLOW }
                this.append(networthCookies.toFormattedString()) { this.color = TextColor.GOLD }
            }

            this.add {
                this.append("Net worth in ${currency.name}: ") { this.color = TextColor.YELLOW }
                val formattedNetworth = networthConverted.roundToInt().toFormattedString()
                this.append("$$formattedNetworth ${currency.name}") { this.color = TextColor.GREEN }
            }

            this.space()
            this.add("Note: You can change the currency in the settings using /sbpv.") { this.color = TextColor.GRAY }
            this.space()
            this.add("Source: ") { this.color = TextColor.GRAY }
            networth.second.forEach {
                this.add {
                    this.append(it.key) { this.color = TextColor.YELLOW }
                    this.append(": ") { this.color = TextColor.YELLOW }
                    this.append(it.value.toFormattedString()) { this.color = TextColor.GREEN }
                }
            }
        }
    }

    fun getNetworthDisplay(profile: SkyBlockProfile): Display = Displays.row(
        Displays.text("Net Worth: ", color = { TextColor.DARK_GRAY.toUInt() }, shadow = false),
        ExtraDisplays.completableDisplay(
            profile.netWorth,
            { Displays.text(it.first.shorten(), color = { TextColor.DARK_GRAY.toUInt() }, shadow = false).addTooltip(it) },
            { error ->
                Displays.text("Failed To Load", color = { TextColor.RED.toUInt() }, shadow = false).withTooltip {
                    this.add(Text.of("An error occurred: ") { this.color = TextColor.RED })
                    error.stackTraceToString().lines().forEach { line ->
                        this.add(Text.of(line) { this.color = TextColor.RED })
                    }
                }
            },
            { Displays.text("Loading...", color = { TextColor.DARK_GRAY.toUInt() }, shadow = false) },
        ),
    )
}
