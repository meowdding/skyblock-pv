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
import me.owdding.skyblockpv.utils.theme.PvColors
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.BazaarAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.roundToLong

object NetworthDisplay {

    private fun Long.toIndianFormat(): String {
        val s = this.toString()
        if (s.length <= 3) return s

        val lastThree = s.substring(s.length - 3)
        val otherNumbers = s.substring(0, s.length - 3)
        val formattedOther = otherNumbers.reversed()
            .chunked(2)
            .joinToString(",")
            .reversed()

        return "$formattedOther,$lastThree"
    }

    private fun Display.addTooltip(networth: Pair<Long, Map<String, Long>>): Display {
        val cookiePrice = BazaarAPI.getProduct("BOOSTER_COOKIE")?.buyPrice ?: 0.0
        val networthCookies = if (cookiePrice > 0) (networth.first / cookiePrice).roundToLong() else 0L
        val networthUSD = ((networthCookies * 325.0) / 675.0) * 4.99

        val (currency, networthConverted) = CurrenciesAPI.convert(Config.currency, networthUSD)

        if (cookiePrice <= 0) return this

        return this.withTooltip {
            this.add {
                this.append(+"widgets.networth.tooltip")
                this.append(networth.first.toFormattedString()) { this.color = PvColors.GREEN }
            }

            this.add {
                this.append(+"widgets.networth.tooltip.cookies")
                this.append(networthCookies.toFormattedString()) { this.color = PvColors.GOLD }
            }

            this.add {
                this.append("widgets.networth.tooltip.currency".asTranslated(currency.name))
                val roundedNetworth = networthConverted.roundToLong()

                this.append(currency.format(roundedNetworth)) { this.color = PvColors.GREEN }

            }

            this.space()
            this.add(+"widgets.networth.tooltip.note")
            this.space()
            this.add(+"widgets.networth.tooltip.sources")
            networth.second.forEach {
                this.add {
                    this.append(it.key) { this.color = PvColors.YELLOW }
                    this.append(": ") { this.color = PvColors.YELLOW }
                    this.append(it.value.toFormattedString()) { this.color = PvColors.GREEN }
                }
            }
        }
    }

    fun getNetworthDisplay(profile: SkyBlockProfile): Display = Displays.row(
        ExtraDisplays.component(+"widgets.networth", color = { PvColors.DARK_GRAY.toUInt() }, shadow = false),
        ExtraDisplays.completableDisplay(
            profile.netWorth,
            { ExtraDisplays.grayText(it.first.shorten()).addTooltip(it) },
            { error ->
                ExtraDisplays.component(+"widgets.networth.failed", color = { PvColors.RED.toUInt() }, shadow = false).withTooltip {
                    this.add {
                        add(+"widgets.networth.error")
                        this.color = PvColors.RED
                    }
                    error.getStackTraceString(10).lines().forEach { line ->
                        this.add(Text.of(line) { this.color = PvColors.RED })
                    }
                }
            },
            { ExtraDisplays.component(+"widgets.networth.loading", color = { PvColors.DARK_GRAY.toUInt() }, shadow = false) },
        ),
    )
}
