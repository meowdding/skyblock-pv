package me.owdding.skyblockpv.feature.networth

import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import java.util.concurrent.CompletableFuture

typealias Networth = Pair<Long, Map<String, Long>>

object NetworthCalculator {

    fun calculateNetworth(profile: SkyBlockProfile): Networth {
        val start = System.currentTimeMillis()
        val total = NetworthCategory.entries.associateWith { it.source.getItemValues(profile) }
        if (SkyBlockPv.isDevMode) SkyBlockPv.info("Networth calculation took ${System.currentTimeMillis() - start}ms")

        val formatted = buildMap<String, Long> {

        }

        return total.values.sumOf { it.values.sum() } to formatted
    }

    fun calculateNetworthAsync(profile: SkyBlockProfile): CompletableFuture<Networth> {
        return CompletableFuture.supplyAsync {
            calculateNetworth(profile)
        }
    }

}
