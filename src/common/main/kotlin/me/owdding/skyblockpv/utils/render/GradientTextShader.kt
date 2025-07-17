package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderPass
import me.owdding.lib.extensions.rightPad
import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.TextColor

class GradientTextShader(gradientProvider: GradientProvider) : TextShader {
    constructor(colors: List<Int>) : this({ colors })
    constructor(vararg colors: TextColor) : this(colors.map { it.value })

    val colors = gradientProvider.getColors().take(16).toMutableList().rightPad(16, 0).map { it.toFloat() }.toFloatArray()
    val states = gradientProvider.getColors().take(16).size
    override val pipeline: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET)
            .withLocation(SkyBlockPv.id("gradient_text"))
            .withVertexShader(SkyBlockPv.id("text/gradient"))
            .withFragmentShader(SkyBlockPv.id("text/gradient"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withDepthBias(-1.0f, -10.0f)
            .build(),
    )

    override fun pass(renderPass: RenderPass) {

    }

    override val useWhite: Boolean get() = true
    override val hasShadow: Boolean? get() = true

}

fun interface GradientProvider {
    fun getColors(): List<Int>
}
