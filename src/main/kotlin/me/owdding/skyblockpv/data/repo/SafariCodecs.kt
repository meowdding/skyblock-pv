package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@LoadData
object SafariCodecs : DefaultedData {
    private val defaultData = Safari(emptyList(), emptyList())
    private var _data: Safari? = null
    val data: Safari get() = _data ?: defaultData

    override suspend fun load() {
        _data = Utils.loadRemoteRepoData<Safari>("pv/safari")
    }

    @GenerateCodec
    data class Safari(
        val milestones: List<Int>,
        val critters: List<Critter>,
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
