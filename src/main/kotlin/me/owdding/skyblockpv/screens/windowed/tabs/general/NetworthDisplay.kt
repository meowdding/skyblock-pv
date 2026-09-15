package me.owdding.skyblockpv.screens.windowed.tabs.general

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.extensions.shorten
import me.owdding.skyblockpv.api.MuseumAPI
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.config.CurrenciesAPI
import me.owdding.skyblockpv.feature.networth.Networth
import me.owdding.skyblockpv.feature.networth.NetworthCategory
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

    private fun Display.addTooltip(networth: Networth, profile: SkyBlockProfile): Display {
        val cookiePrice = BazaarAPI.getProduct("BOOSTER_COOKIE")?.buyPrice ?: 0.0
        val cookies = if (cookiePrice > 0) networth.first / cookiePrice else 0.0
        val networthCookies = cookies.roundToLong()
        val networthUSD = ((cookies * 325.0) / 675.0) * 4.99

        val (currency, networthConverted) = CurrenciesAPI.convert(Config.currency, networthUSD)

        if (cookiePrice <= 0) return this

        val hasMuseum = profile.onStranded || MuseumAPI.getCached(profile) != null

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
                this.append(currency.format(networthConverted)) { this.color = PvColors.GREEN }
            }

            this.space()
            this.add(+"widgets.networth.tooltip.note")
            this.space()
            this.add(+"widgets.networth.tooltip.sources")
            networth.second.forEach { (category, map) ->
                this.add {
                    this.append(category.toString()) { this.color = PvColors.YELLOW }
                    this.append(": ") { this.color = PvColors.YELLOW }
                    if (category == NetworthCategory.MUSEUM && !hasMuseum) {
                        this.append(+"widgets.networth.tooltip.not_loaded")
                    } else {
                        val categoryTotal = map.values.sum()
                        this.append(categoryTotal.toFormattedString()) { this.color = PvColors.GREEN }
                    }
                }
            }

            if (profile.onStranded) {
                this.space()
                this.add(+"widgets.networth.tooltip.stranded_museum_hint")
            } else if (!hasMuseum) {
                this.space()
                this.add(+"widgets.networth.tooltip.museum_hint")
            }
        }
    }

    fun getNetworthDisplay(profile: SkyBlockProfile): Display = Displays.row(
        ExtraDisplays.component(+"widgets.networth", color = { PvColors.DARK_GRAY.toUInt() }, shadow = false),
        ExtraDisplays.completableDisplay(
            profile.netWorth,
            { networth ->
                val hasMuseum = profile.onStranded || MuseumAPI.getCached(profile) != null
                val text = if (hasMuseum) networth.first.shorten() else "${networth.first.shorten()}*"
                ExtraDisplays.grayText(text).addTooltip(networth, profile)
            },
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

    fun networthDebug(profile: SkyBlockProfile): List<String> = buildList {
        fun addLine(name: String, amount: Number?) = add(" - $name: ${amount?.toFormattedString()}")

        add("# Networth Breakdown: ${profile.netWorth.get().first.toFormattedString()}")

        profile.netWorth.get().second.forEach { (category, map) ->
            val categoryTotal = map.values.sum()
            add("## $category - ${categoryTotal.toFormattedString()}")
            map.toList().sortedByDescending(Pair<String, Long>::second).take(20).forEach { (itemName, itemValue) ->
                addLine(itemName, itemValue)
            }
        }
    }
}
