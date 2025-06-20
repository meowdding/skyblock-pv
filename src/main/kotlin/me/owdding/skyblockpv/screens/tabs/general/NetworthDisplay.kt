package me.owdding.skyblockpv.screens.tabs.general

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.extensions.shorten
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.config.CurrenciesAPI
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.unaryPlus
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
                this.append(+"widgets.networth.tooltip")
                this.append(networth.first.toFormattedString()) { this.color = TextColor.GREEN }
            }

            this.add {
                this.append(+"widgets.networth.tooltip.cookies")
                this.append(networthCookies.toFormattedString()) { this.color = TextColor.GOLD }
            }

            this.add {
                this.append("widgets.networth.tooltip.currency".asTranslated(currency.name))
                val formattedNetworth = networthConverted.roundToInt().toFormattedString()
                this.append("$formattedNetworth ${currency.name}") { this.color = TextColor.GREEN }
            }

            this.space()
            this.add(+"widgets.networth.tooltip.note")
            this.space()
            this.add(+"widgets.networth.tooltip.sources")
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

        Displays.component(+"widgets.networth", shadow = false),
        ExtraDisplays.completableDisplay(
            profile.netWorth,
            { Displays.text(it.first.shorten(), color = { TextColor.DARK_GRAY.toUInt() }, shadow = false).addTooltip(it) },
            { error ->
                Displays.component(+"widgets.networth.failed", shadow = false).withTooltip {
                    this.add(+"widgets.networth.error")
                    error.getStackTraceString(10).lines().forEach { line ->
                        this.add(Text.of(line) { this.color = TextColor.RED })
                    }
                }
            },
            { Displays.component(+"widgets.networth.loading", shadow = false) },
        ),
    )
}
