package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockpv.utils.Utils

@Module
object SlayerCodecs {
    var data: Map<String, Slayer> = emptyMap()
        private set

    init {
        val CODEC = Codec.unboundedMap(Codec.STRING, Slayer.CODEC)
        data = Utils.loadFromRepo<JsonObject>("slayer").toData(CODEC) ?: throw RuntimeException("Failed to load slayer data")
    }

    data class Slayer(
        val name: String,
        val id: String,
        val leveling: List<Long>,
        val bossXp: List<Int>,
    ) {
        val maxBossTier = bossXp.size
        val maxLevel = leveling.size

        fun getLevel(xp: Long) = leveling.indexOfLast { it < xp } + 1

        companion object {
            val CODEC = RecordCodecBuilder.create {
                it.group(
                    Codec.STRING.fieldOf("name").forGetter(Slayer::name),
                    Codec.STRING.fieldOf("id").forGetter(Slayer::id),
                    Codec.LONG.listOf().fieldOf("leveling").forGetter(Slayer::leveling),
                    Codec.INT.listOf().fieldOf("xp").forGetter(Slayer::bossXp),
                ).apply(it, ::Slayer)
            }
        }
    }
}
