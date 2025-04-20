package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.JsonArray
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockpv.utils.Utils

@Module
object SackCodecs {
    var data: List<Sack>
        private set

    init {
        data = Utils.loadFromRepo<JsonArray>("sacks").toData(Sack.CODEC.listOf()) ?: throw IllegalStateException("Failed to load sacks data!")
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
