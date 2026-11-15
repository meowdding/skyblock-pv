package me.owdding.skyblockpv.api.pronouns

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.rendering.text.TextShader
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.network.chat.TextColor

@Module
object PronounDbDecorations {

    private val decorations: Map<String, DecorationDefinition> by SkyBlockPv.repoDelegate("pv/pronoun_decorations", emptyMap())

    fun getShader(id: String): TextShader? = decorations[id]?.shader
}

data class DecorationDefinition(
    val colors: List<TextColor>,
) {

    val shader by lazy { GradientTextShader(*colors.toTypedArray()) }

    companion object {
        @IncludedCodec
        val CODEC: Codec<DecorationDefinition> = TextColor.CODEC.listOf().xmap(::DecorationDefinition, DecorationDefinition::colors)
    }
}
