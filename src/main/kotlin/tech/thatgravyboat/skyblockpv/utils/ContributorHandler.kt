package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.coroutines.runBlocking
import net.minecraft.core.UUIDUtil
import net.minecraft.world.entity.animal.Parrot
import java.util.*
import kotlin.jvm.optionals.getOrNull

object ContributorHandler {
    var contributors: Map<UUID, ContributorData> = emptyMap()
        private set

    init {
        runBlocking {
            try {
                contributors = Utils.loadFromRepo<JsonObject>("contributors")?.let {
                    val parse = Codec.unboundedMap(UUIDUtil.STRING_CODEC, ContributorData.CODEC).parse(JsonOps.INSTANCE, it)
                    if (parse.isError) {
                        throw UnsupportedOperationException(parse.error().get().message())
                    }

                    parse.getOrThrow()
                } ?: mutableMapOf()
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}

data class ContributorData(
    val title: String?,
    val parrotLeft: Parrot.Variant?,
    val parrotRight: Parrot.Variant?,
    val shaking: Boolean,
) {
    companion object {
        val CODEC: Codec<ContributorData> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.optionalFieldOf("title").forGetter { Optional.empty() },
                Parrot.Variant.CODEC.optionalFieldOf("parrot_left").forGetter { Optional.empty() },
                Parrot.Variant.CODEC.optionalFieldOf("parrot_right").forGetter { Optional.empty() },
                Codec.BOOL.optionalFieldOf("shaking", false).forGetter(ContributorData::shaking),
            ).apply(it, ::init)
        }

        fun init(
            title: Optional<String>,
            parrotLeft: Optional<Parrot.Variant>,
            parrotReft: Optional<Parrot.Variant>,
            shaking: Boolean,
        ): ContributorData {
            return ContributorData(title.getOrNull(), parrotLeft.getOrNull(), parrotReft.getOrNull(), shaking)
        }
    }
}
