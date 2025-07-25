package me.owdding.skyblockpv.utils.render

import net.minecraft.Util
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderType.CompositeState
import net.minecraft.resources.ResourceLocation
import java.util.function.BiFunction

val TEXT_RENDER_TYPE_CACHE: BiFunction<TextShader, ResourceLocation, RenderType> =
    Util.memoize<TextShader, ResourceLocation, RenderType> { shader, location ->
        RenderType.create(
            "skyblockpv/",
            786432,
            false,
            false,
            shader.pipeline,
            CompositeState.builder()
                .setTextureState(TextureStateShard(location, false))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .createCompositeState(false),
        )
    }

actual fun createTextRenderType(shader: TextShader, location: ResourceLocation): RenderType =
    TEXT_RENDER_TYPE_CACHE.apply(shader, location)
