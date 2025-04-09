package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.utils.Utils

object SackCodecs {
    var data: Map<String, Sack> = emptyMap()
        private set

    init {
        val CODEC = Codec.unboundedMap(Codec.STRING, Sack.CODEC)

        val cfData = Utils.loadFromRepo<JsonObject>("sacks") ?: JsonObject()

        CODEC.parse(JsonOps.INSTANCE, cfData).let {
            if (it.isError) {
                throw RuntimeException(it.error().get().message())
            }
            data = it.getOrThrow()
            println(data)
        }
    }

    data class Sack(
        val sack: String,
        val items: List<String>,
    ) {
        val item by lazy { ItemAPI.getItem(sack) }

        companion object {
            val CODEC = RecordCodecBuilder.create {
                it.group(
                    Codec.STRING.fieldOf("sack").forGetter(Sack::sack),
                    Codec.STRING.listOf().fieldOf("items").forGetter(Sack::items),
                ).apply(it, ::Sack)
            }
        }
    }
}
