package me.owdding.skyblockpv.screens.tabs.main

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.DisplayWidget
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.feature.NetworthCalculator.calculateNetworth
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.pricing.Pricing
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class NetworthScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMainScreen(gameProfile, profile) {
    private val purseComponent = Text.of("Purse") { color = TextColor.GOLD }
    private val profileBankComponent = Text.of("Profile Bank") { color = TextColor.GOLD }
    private val soloBankComponent = Text.of("Solo Bank") { color = TextColor.GOLD }

    override fun getLayout(bg: DisplayWidget): Layout {
        return LayoutFactory.vertical {
            getNetworthSources(profile).forEach { (string, amount) ->
                textDisplay {
                    append(string)
                    append(" - ")
                    append(amount.toFormattedString())
                }
            }
        }.asScrollable(uiWidth, uiHeight)
    }


    private fun getNetworthSources(profile: SkyBlockProfile) = buildMap<Component, Long> {
        profile.inventory?.getAllItems()?.forEach { item ->
            val name = if (item.count > 1) Text.join("§7${item.count}x ", item.hoverName) else item.hoverName
            put(name, item.getItemValue().price)
        }
        profile.inventory?.sacks?.forEach { sack ->
            val name = if (sack.value > 1) Text.join("§7${sack.value.toFormattedString()}x ", RepoItemsAPI.getItemName(sack.key))
            else RepoItemsAPI.getItemName(sack.key)
            put(name, Pricing.getPrice(sack.key) * sack.value)
        }
        profile.pets.forEach { pet ->
            put(pet.itemStack.hoverName, pet.calculateNetworth())
        }
        put(purseComponent, profile.currency?.purse ?: 0L)
        put(profileBankComponent, profile.bank?.profileBank ?: 0L)
        put(soloBankComponent, profile.bank?.soloBank ?: 0L)
    }.filterValues { it > 0L }.toList().sortedByDescending { it.second }.map { it.first to it.second }
}
