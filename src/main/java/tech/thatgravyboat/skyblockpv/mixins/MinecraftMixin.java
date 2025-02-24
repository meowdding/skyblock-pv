package tech.thatgravyboat.skyblockpv.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tech.thatgravyboat.skyblockpv.screens.ForcedGuiScaleScreen;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @WrapOperation(method = "resizeDisplay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;calculateScale(IZ)I"))
    private int overrideGuiScale(Window instance, int guiScale, boolean forceUnicode, Operation<Integer> original) {
        guiScale = ForcedGuiScaleScreen.isInForcedScaleGui() ? Math.min(guiScale, 2) : guiScale;
        return original.call(instance, guiScale, forceUnicode);
    }
}
