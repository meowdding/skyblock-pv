package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyblockpv.Helper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class FontMixin {

    @WrapOperation(method = "finish", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;renderType(Lnet/minecraft/client/gui/Font$DisplayMode;)Lnet/minecraft/client/renderer/RenderType;", ordinal = 0))
    public RenderType modify(BakedGlyph instance, Font.DisplayMode displayMode, Operation<RenderType> original) {
        Helper.skipTextShader = true;
        var returnValue = original.call(instance, displayMode);
        Helper.skipTextShader = false;
        return returnValue;
    }
}
