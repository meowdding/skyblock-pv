package me.owdding.skyblockpv.feature

import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.api.remote.pricing.LowestBinAPI
import tech.thatgravyboat.skyblockapi.api.remote.pricing.Pricing
import java.util.concurrent.CompletableFuture

typealias Networth = Pair<Long, Map<String, Long>>

object NetworthCalculator {

    fun calculateNetworth(profile: SkyBlockProfile): Networth {
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

        val total = mapOf(
            "Inventory/Enderchest/Backpack/..." to items,
            "Sacks" to sacks,
            "Pets" to pets,
            "Purse/Bank" to listOfNotNull(profile.currency?.purse, profile.bank?.profileBank, profile.bank?.soloBank).sum(),
        )

        return total.entries.sumOf { it.value } to total
    }

    fun calculateNetworthAsync(profile: SkyBlockProfile): CompletableFuture<Networth> {
        return CompletableFuture.supplyAsync {
            calculateNetworth(profile)
        }
    }

}
