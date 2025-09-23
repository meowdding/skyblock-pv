package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import me.owdding.lib.utils.MeowddingPipelines
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.Utils.withShaderDefine
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.TextColor

class GradientTextShader(gradientProvider: GradientProvider) : TextShader {
    constructor(colors: List<Int>) : this({ colors })
    constructor(vararg colors: TextColor) : this(colors.map { it.value })

    override val pipeline: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET, MeowddingPipelines.GAME_TIME_SNIPPET)
            .withLocation(SkyBlockPv.id("gradient_text"))
            .withVertexShader(SkyBlockPv.id("text/gradient"))
            .withFragmentShader(SkyBlockPv.id("text/gradient"))
            .withSampler("Sampler0")
            .withDepthBias(-1.0f, -10.0f)
            .withShaderDefine("COLORS", gradientProvider.getColors().toIntArray())
            .build(),
    )

    override val useWhite: Boolean get() = true
    override val hasShadow: Boolean? get() = true

}

fun interface GradientProvider {
    fun getColors(): List<Int>
}
