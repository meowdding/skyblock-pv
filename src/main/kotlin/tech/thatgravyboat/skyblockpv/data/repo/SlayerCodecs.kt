package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import tech.thatgravyboat.skyblockpv.data.repo.SlayerCodecs.Slayer
import tech.thatgravyboat.skyblockpv.utils.Utils

object SlayerCodecs {
    var data: Map<String, Slayer> = emptyMap()
        private set

    init {
        val CODEC = Codec.unboundedMap(Codec.STRING, Slayer.CODEC)

        val slayerData = Utils.loadFromRepo<JsonObject>("slayer") ?: JsonObject()

        CODEC.parse(JsonOps.INSTANCE, slayerData).let {
            if (it.isError) {
                throw RuntimeException(it.error().get().message())
            }
            data = it.getOrThrow()
            println(data)
        }
    }

    data class Slayer(
        val name: String,
        val id: String,
        val leveling: List<Long>,
        val xp: List<Int>,
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.create {
                it.group(
                    Codec.STRING.fieldOf("name").forGetter(Slayer::name),
                    Codec.STRING.fieldOf("id").forGetter(Slayer::id),
                    Codec.LONG.listOf().fieldOf("leveling").forGetter(Slayer::leveling),
                    Codec.INT.listOf().fieldOf("xp").forGetter(Slayer::xp),
                ).apply(it, ::Slayer)
            }
        }
    }
}
