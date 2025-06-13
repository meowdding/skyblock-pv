package me.owdding.skyblockpv.utils

import kotlinx.coroutines.runBlocking
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.render.GradientTextShader
import net.minecraft.core.ClientAsset
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.world.entity.animal.Parrot
import java.util.*

object ContributorHandler {
    var contributors: Map<UUID, ContributorData> = emptyMap()
        private set

    init {
        runBlocking {
            try {
                contributors = Utils.loadRepoData("contributors", CodecUtils.map<UUID, ContributorData>())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


@GenerateCodec
data class ContributorData(
    @NamedCodec("component_tag") val title: Component?,
    val parrot: ParrotOnShoulder?,
    val cat: CatOnShoulder?,
    val shaking: Boolean = false,
    @FieldName("title_colors") val tileColors: List<TextColor>?,
) {
    val titleShader = tileColors?.let { GradientTextShader(*it.toTypedArray()) }
}

@GenerateCodec
data class ParrotOnShoulder(val variant: Parrot.Variant, @FieldName("left_shoulder") val leftSide: Boolean)

@GenerateCodec
data class CatOnShoulder(@FieldName("asset_id") val asset: ClientAsset, @FieldName("left_shoulder") val leftSide: Boolean)
