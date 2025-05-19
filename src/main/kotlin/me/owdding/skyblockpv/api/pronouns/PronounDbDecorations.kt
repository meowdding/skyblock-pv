package me.owdding.skyblockpv.api.pronouns

import com.mojang.serialization.Codec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import me.owdding.skyblockpv.utils.render.GradientTextShader
import me.owdding.skyblockpv.utils.render.TextShader
import net.minecraft.network.chat.TextColor

@LoadData
object PronounDbDecorations : ExtraData {

    private val CODEC: Codec<Map<String, DecorationDefinition>> = Codec.unboundedMap(
        Codec.STRING,
        TextColor.CODEC.listOf().xmap(::DecorationDefinition, DecorationDefinition::colors),
    )

    private var decorations: Map<String, DecorationDefinition> = emptyMap()

    override suspend fun load() {
        runCatching {
            decorations = Utils.loadRepoData("pronoun_decorations", CODEC)
        }
    }

    fun getShader(id: String): TextShader? = decorations[id]?.shader ?: decorations[id]?.shader
}

data class DecorationDefinition(
    val colors: List<TextColor>,
) {

    val shader by lazy { GradientTextShader(*colors.toTypedArray()) }

}
