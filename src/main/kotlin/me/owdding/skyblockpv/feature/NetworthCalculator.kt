package me.owdding.skyblockpv.feature

import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockapi.api.area.hub.LowestBinAPI
import tech.thatgravyboat.skyblockapi.api.item.calculator.Pricing
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import java.util.concurrent.CompletableFuture

object NetworthCalculator {

    fun calculateNetworth(profile: SkyBlockProfile): Long {
        val start = System.currentTimeMillis()
        val items = profile.inventory?.getAllItems()?.sumOf { it.getItemValue().price } ?: 0
        val sacks = profile.inventory?.sacks?.entries?.sumOf { Pricing.getPrice(it.key) * it.value } ?: 0
        val pets = profile.pets.sumOf {
            val pet = LowestBinAPI.getLowestPrice("pet:${it.type}:${it.rarity}") ?: 0L
            val item = Pricing.getPrice(it.heldItem)
            val skin = Pricing.getPrice(it.skin)
            pet + item + skin
        }
        if (SkyBlockPv.isDevMode) SkyBlockPv.info("Networth calculation took ${System.currentTimeMillis() - start}ms")

        return listOfNotNull(
            items,
            sacks,
            pets,
            profile.currency?.purse,
            profile.bank?.profileBank,
            profile.bank?.soloBank,
        ).sum()
    }

    fun calculateNetworthAsync(profile: SkyBlockProfile): CompletableFuture<Long> {
        return CompletableFuture.supplyAsync {
            calculateNetworth(profile)
        }
    }

}
