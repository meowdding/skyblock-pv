package me.owdding.skyblockpv.feature.networth

import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import java.util.concurrent.CompletableFuture

typealias Networth = Pair<Long, Map<NetworthCategory, Map<String, Long>>>

object NetworthCalculator {

    fun calculateNetworth(profile: SkyBlockProfile): Networth {
        val start = System.currentTimeMillis()
        val total = NetworthCategory.entries.associateWith { it.source.getItemValues(profile) }
        if (SkyBlockPv.isDevMode) SkyBlockPv.info("Networth calculation took ${System.currentTimeMillis() - start}ms")
        return total.values.sumOf { it.values.sum() } to total
    }

    fun calculateNetworthAsync(profile: SkyBlockProfile): CompletableFuture<Networth> {
        return profile.dataFuture.thenApplyAsync {
            calculateNetworth(profile)
        }
    }

}
