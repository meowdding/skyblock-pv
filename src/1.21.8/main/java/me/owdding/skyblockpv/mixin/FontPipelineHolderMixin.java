package me.owdding.skyblockpv.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import me.owdding.skyblockpv.accessor.FontPipelineHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = {
    "net.minecraft.client.gui.render.state.GuiTextRenderState",
    "net.minecraft.client.gui.font.glyphs.BakedGlyph$GlyphInstance",
    "net.minecraft.client.gui.font.glyphs.BakedGlyph$Effect",
    "net.minecraft.client.gui.font.Font$PreparedTextBuilder",
})
public class FontPipelineHolderMixin implements FontPipelineHolder {

    @Unique
    private RenderPipeline sbpv$pipeline;

    @Override
    public RenderPipeline skyblockpv$getPipeline() {
        return this.sbpv$pipeline;
    }

    @Override
    public void skyblockpv$setPipeline(RenderPipeline pipeline) {
        this.sbpv$pipeline = pipeline;
    }
}
