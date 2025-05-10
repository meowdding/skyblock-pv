package me.owdding.skyblockpv.api.pronouns

import com.mojang.serialization.Codec
import com.teamresourceful.resourcefullib.common.color.Color
import eu.pb4.placeholders.api.node.parent.GradientNode
import eu.pb4.placeholders.api.node.parent.GradientNode.GradientProvider
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor

@LoadData
object PronounDbDecorations : ExtraData {

    private val CODEC: Codec<Map<String, DecorationDefinition>> = Codec.unboundedMap(
        Codec.STRING,
        TextColor.CODEC.listOf().xmap(::DecorationDefinition, DecorationDefinition::colors)
    )

    private var decorations: Map<String, DecorationDefinition> = emptyMap()

    override suspend fun load() {
        runCatching {
            decorations = Utils.loadRepoData<Map<String, DecorationDefinition>>("pronoun_decorations", CODEC)
        }
    }

    fun formatColor(decoration: String?, text: Component): Component {
        return this.decorations[decoration]?.let { definition -> GradientNode.apply(text, definition) } ?: text
    }
}

data class DecorationDefinition(
    val colors: List<TextColor>,
) : GradientProvider {

    private val provider by lazy { GradientProvider.colorsHard(this.colors) }

    override fun getColorAt(index: Int, length: Int): TextColor {
        if (colors.isEmpty()) return Color.DEFAULT.textColor
        return provider.getColorAt(index, length)
    }

}
