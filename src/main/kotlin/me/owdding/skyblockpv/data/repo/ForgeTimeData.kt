package me.owdding.skyblockpv.data.repo

import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.api.data.Perk
import tech.thatgravyboat.skyblockapi.api.remote.RepoRecipeAPI
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Module
object ForgeTimeData {
    private val quickForgeMultiplier = mapOf(
        1 to 0.895,
        2 to 0.89,
        3 to 0.885,
        4 to 0.88,
        5 to 0.875,
        6 to 0.87,
        7 to 0.865,
        8 to 0.86,
        9 to 0.855,
        10 to 0.85,
        11 to 0.845,
        12 to 0.84,
        13 to 0.835,
        14 to 0.83,
        15 to 0.825,
        16 to 0.82,
        17 to 0.815,
        18 to 0.81,
        19 to 0.805,
        20 to 0.7,
    )

    private fun coleActive() = Perk.MOLTEN_FORGE.active

    fun getForgeTime(id: String, quickForgeLevel: Int = 0): Duration {
        val rawTime = RepoRecipeAPI.getForgeRecipe(id)?.time?.times(1000) ?: 0
        val quickForgeMultiplier = quickForgeMultiplier[quickForgeLevel] ?: 1.0
        return (rawTime * quickForgeMultiplier * if (coleActive()) 0.75 else 1.0).toDuration(DurationUnit.MILLISECONDS)
    }
}
