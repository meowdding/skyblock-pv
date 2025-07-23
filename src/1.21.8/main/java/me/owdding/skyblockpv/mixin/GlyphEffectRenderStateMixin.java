package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import me.owdding.skyblockpv.accessor.FontPipelineHolder;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.render.state.GlyphEffectRenderState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GlyphEffectRenderState.class)
public class GlyphEffectRenderStateMixin {

    @Shadow
    @Final
    @NotNull
    private BakedGlyph.Effect effect;

    @WrapMethod(method = "pipeline")
    private RenderPipeline sbpv$usePipeline(Operation<RenderPipeline> original) {
        var holder = FontPipelineHolder.getHolder(this.effect);
        var pipeline = holder != null ? holder.skyblockpv$getPipeline() : null;
        return pipeline != null ? pipeline : original.call();
    }
}
