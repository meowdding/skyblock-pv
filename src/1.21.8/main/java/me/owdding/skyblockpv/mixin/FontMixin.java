package me.owdding.skyblockpv.mixin;

import me.owdding.skyblockpv.MixinHelper;
import me.owdding.skyblockpv.accessor.FontPipelineHolder;
import me.owdding.skyblockpv.utils.render.RenderUtils;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public class FontMixin {

    @Unique
    private final int skyblockpv$shadow = ARGB.scaleRGB(0xFFFFFFFF, 0.25f);

    @Inject(method = "addGlyph", at = @At("HEAD"))
    private void apply(BakedGlyph.GlyphInstance instance, CallbackInfo ci) {
        var holder = FontPipelineHolder.getHolder(instance);
        if (holder == null) return;
        holder.skyblockpv$setPipeline(MixinHelper.FONT_PIPELINE.get());
    }

    @Inject(method = "addEffect", at = @At("HEAD"))
    private void apply(BakedGlyph.Effect effect, CallbackInfo ci) {
        var holder = FontPipelineHolder.getHolder(effect);
        if (holder == null) return;
        holder.skyblockpv$setPipeline(MixinHelper.FONT_PIPELINE.get());
    }

    @Inject(method = "getTextColor", at = @At("HEAD"), cancellable = true)
    public void getTextColor(TextColor textColor, CallbackInfoReturnable<Integer> cir) {
        var shader = RenderUtils.INSTANCE.getTEXT_SHADER();
        if (shader != null && shader.getUseWhite()) {
            cir.setReturnValue(0xFFFFFFFF);
        }
    }

    @Inject(method = "getShadowColor", at = @At("RETURN"), cancellable = true)
    public void getShadowColor(CallbackInfoReturnable<Integer> cir) {
        var shader = RenderUtils.INSTANCE.getTEXT_SHADER();
        if (shader == null) return;
        var shadow = shader.getHasShadow();
        if (shadow != null && !shadow) {
            cir.setReturnValue(0);
        }
        if (shader.getUseWhite()) {
            cir.setReturnValue(skyblockpv$shadow);
        }
    }
}
