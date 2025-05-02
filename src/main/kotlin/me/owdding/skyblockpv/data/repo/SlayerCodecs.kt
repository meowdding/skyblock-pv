package me.owdding.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs
import me.owdding.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow

@Module
object SlayerCodecs {
    var data: Map<String, Slayer> = emptyMap()
        private set

    init {
        val codec: Codec<Map<String, Slayer>> = Codec.unboundedMap(Codec.STRING, SkyBlockPVCodecs.getCodec<Slayer>())
        data = Utils.loadFromRepo<JsonObject>("slayer").toDataOrThrow(codec)
    }

    @GenerateCodec
    data class Slayer(
        val name: String,
        val id: String,
        val leveling: List<Long>,
        @FieldName("xp") val bossXp: List<Int>,
    ) {
        val maxBossTier = bossXp.size
        val maxLevel = leveling.size

        fun getLevel(xp: Long) = leveling.indexOfLast { it < xp } + 1
    }
}
