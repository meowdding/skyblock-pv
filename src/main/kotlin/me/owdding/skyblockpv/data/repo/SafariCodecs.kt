package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@Module
object SafariCodecs {
    val data: Safari by SkyBlockPv.repoDelegate("pv/safari", Safari())

    @GenerateCodec
    data class Safari(
        val milestones: List<Int> = emptyList(),
        val critters: List<Critter> = emptyList(),
    )

    @GenerateCodec
    data class Critter(
        val id: String,
        val name: String,
        val attribute: SkyBlockId,
        val biome: CritterSafariBiome,
    )

    enum class CritterSafariBiome(val color: Int) {
        CAVERN(TextColor.GOLD),
        FOREST(TextColor.DARK_GREEN),
        HAUNTED(TextColor.DARK_PURPLE),
        ICY(TextColor.BLUE),
        ;

        val formattedName = toFormattedName()
        val component = Text.of(formattedName, color)
    }

    enum class SafariTicket(val color: Int, formattedName: String? = null) {
        BASIC(TextColor.DARK_GREEN),
        ECONOMY(TextColor.BLUE),
        PREMIUM(TextColor.DARK_PURPLE),
        FIRST_CLASS(TextColor.GOLD, "First-Class"),
        ;

        val formattedName = formattedName ?: toFormattedName()
    }
}
