@file:Suppress("unused")

package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.msrandom.stub.Stub

@Stub
expect fun createTextRenderType(
    shader: TextShader,
    location: ResourceLocation,
): RenderType

interface TextShader {

    val pipeline: RenderPipeline

    val useWhite: Boolean get() = true
    val hasShadow: Boolean? get() = null

    fun getRenderType(location: ResourceLocation): RenderType {
        return createTextRenderType(this, location)
    }
}
