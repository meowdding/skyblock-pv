package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyblockpv.MixinHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class FontMixin {

    @Unique
    private final int skyblockpv$shadow = ARGB.scaleRGB(0xFFFFFFFF, 0.25f);

    @WrapOperation(method = "finish", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;renderType(Lnet/minecraft/client/gui/Font$DisplayMode;)Lnet/minecraft/client/renderer/RenderType;", ordinal = 0))
    public RenderType modify(BakedGlyph instance, Font.DisplayMode displayMode, Operation<RenderType> original) {
        MixinHelper.skipTextShader = true;
        var returnValue = original.call(instance, displayMode);
        MixinHelper.skipTextShader = false;
        return returnValue;
    }

    @Inject(method = "getTextColor", at = @At("HEAD"), cancellable = true)
    public void getTextColor(TextColor textColor, CallbackInfoReturnable<Integer> cir) {

    }

    @Inject(method = "getShadowColor", at = @At("RETURN"), cancellable = true)
    public void getShadowColor(CallbackInfoReturnable<Integer> cir) {

    }
}
