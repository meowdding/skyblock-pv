package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockpv.utils.Utils

object SackCodecs {
    var data: List<Sack> = emptyList()
        private set

    init {
        val CODEC = Sack.CODEC.listOf()

        val sackData = Utils.loadFromRepo<JsonArray>("sacks") ?: JsonObject()

        CODEC.parse(JsonOps.INSTANCE, sackData).let {
            if (it.isError) {
                throw RuntimeException(it.error().get().message())
            }
            data = it.getOrThrow()
        }
    }

    data class Sack(
        val sack: String,
        val items: List<String>,
    ) {
        val item by lazy { RepoItemsAPI.getItem(sack) }

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
