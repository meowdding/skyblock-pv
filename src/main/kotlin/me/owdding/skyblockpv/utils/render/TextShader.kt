@file:Suppress("unused")

package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderPass
import net.minecraft.Util
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderType.CompositeState
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.TriState
import java.util.function.BiFunction

interface TextShader {

    val pipeline: RenderPipeline
    fun pass(renderPass: RenderPass)

    val useWhite: Boolean get() = true
    val hasShadow: Boolean? get() = null

    companion object {
        val TEXT_RENDER_TYPE: BiFunction<TextShader, ResourceLocation, RenderType> =
            Util.memoize<TextShader, ResourceLocation, RenderType> { shader, location ->
                CustomShaderRenderType(
                    RenderType.create(
                        "skyblockpv/",
                        786432,
                        false,
                        false,
                        shader.pipeline,
                        CompositeState.builder().setTextureState(TextureStateShard(location, TriState.FALSE, false))
                            .setLightmapState(RenderStateShard.LIGHTMAP).createCompositeState(false),
                    ),
                    shader::pass,
                )
            }
    }
}
