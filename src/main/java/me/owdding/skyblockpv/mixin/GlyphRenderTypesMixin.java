package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.owdding.skyblockpv.MixinHelper;
import me.owdding.skyblockpv.utils.render.RenderUtils;
import me.owdding.skyblockpv.utils.render.TextShader;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlyphRenderTypes.class)
public class GlyphRenderTypesMixin {

    @Unique
    private ResourceLocation skyblockpv$texture;

    @ModifyReturnValue(method = {"createForColorTexture", "createForIntensityTexture"}, at = @At("RETURN"))
    private static GlyphRenderTypes create(GlyphRenderTypes original, @Local(argsOnly = true) ResourceLocation texture) {
        ((GlyphRenderTypesMixin) ((Object) original)).skyblockpv$texture = texture;
        return original;
    }

    @Inject(method = "select", at = @At("HEAD"), cancellable = true)
    private void select(CallbackInfoReturnable<RenderType> cir) {
        if (MixinHelper.skipTextShader) return;
        var shader = RenderUtils.INSTANCE.getTEXT_SHADER();
        if (shader != null) {
            cir.setReturnValue(TextShader.Companion.getTEXT_RENDER_TYPE().apply(shader, skyblockpv$texture));
        }
    }

}
