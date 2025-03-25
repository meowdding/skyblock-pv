package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.coroutines.runBlocking
import net.minecraft.core.UUIDUtil
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
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
    val parrot: ParrotOnShoulder?,
    val cat: CatOnShoulder?,
    val shaking: Boolean,
) {
    companion object {
        private val PARROT_CODEC = RecordCodecBuilder.create {
            it.group(
                Parrot.Variant.CODEC.fieldOf("variant").forGetter(ParrotOnShoulder::variant),
                Codec.BOOL.fieldOf("left_shoulder").forGetter(ParrotOnShoulder::leftSide),
            ).apply(it, ::ParrotOnShoulder)
        }
        private val CAT_CODEC = RecordCodecBuilder.create {
            it.group(
                ResourceLocation.CODEC.xmap(
                    {
                        BuiltInRegistries.CAT_VARIANT.getOptional(it).orElse(null)?.texture ?: ResourceLocation.parse("meow")
                    },
                    { it },
                ).fieldOf("type").forGetter(CatOnShoulder::type),
                Codec.BOOL.fieldOf("left_shoulder").forGetter(CatOnShoulder::leftSide)
            ).apply(it, ::CatOnShoulder)
        }

        val CODEC: Codec<ContributorData> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.optionalFieldOf("title").forGetter { Optional.empty() },
                PARROT_CODEC.optionalFieldOf("parrot").forGetter { Optional.empty() },
                CAT_CODEC.optionalFieldOf("cat").forGetter { Optional.empty() },
                Codec.BOOL.optionalFieldOf("shaking", false).forGetter(ContributorData::shaking),
            ).apply(it, ::init)
        }

        fun init(
            title: Optional<String>,
            parrot: Optional<ParrotOnShoulder>,
            cat: Optional<CatOnShoulder>,
            shaking: Boolean,
        ): ContributorData {
            if (parrot.isPresent && cat.isPresent && parrot.get().leftSide == cat.get().leftSide) {
                return ContributorData(title.getOrNull(), null, cat.get(), shaking)
            }

            return ContributorData(title.getOrNull(), parrot.getOrNull(), cat.getOrNull(), shaking)
        }
    }

}

data class ParrotOnShoulder(val variant: Parrot.Variant, val leftSide: Boolean)
data class CatOnShoulder(val type: ResourceLocation, val leftSide: Boolean)
