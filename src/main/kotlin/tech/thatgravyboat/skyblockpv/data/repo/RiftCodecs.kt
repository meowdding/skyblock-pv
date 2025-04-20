package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockpv.utils.Utils

@Module
object RiftCodecs {
    var data: RiftRepoData
        private set

    private val trophyCodec = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("id").forGetter(TrophyRepo::id),
            Codec.STRING.fieldOf("name").forGetter(TrophyRepo::name),
        ).apply(it, ::TrophyRepo)
    }

    init {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                trophyCodec.listOf().fieldOf("trophies").forGetter(RiftRepoData::trophies),
                Codec.STRING.listOf().fieldOf("montezuma").forGetter(RiftRepoData::montezuma),
                Codec.STRING.listOf().fieldOf("eyes").forGetter(RiftRepoData::eyes),
            ).apply(it, ::RiftRepoData)
        }

        data = Utils.loadFromRepo<JsonObject>("rift").toData(CODEC) ?: throw IllegalStateException("Failed to load rift data!")
    }

    data class RiftRepoData(
        val trophies: List<TrophyRepo>,
        val montezuma: List<String>,
        val eyes: List<String>,
    )

    data class TrophyRepo(
        val id: String,
        val name: String,
    ) {
        val hypixelId = "RIFT_TROPHY_${id.uppercase()}"
        val item by lazy { RepoItemsAPI.getItem(hypixelId) }
    }
}
