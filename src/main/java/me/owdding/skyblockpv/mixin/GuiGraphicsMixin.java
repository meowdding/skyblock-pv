package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyblockpv.mixin.accessors.ScissorStackAccessor;
import me.owdding.skyblockpv.utils.accessors.GuiGraphicsAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
abstract public class GuiGraphicsMixin implements GuiGraphicsAccessor {
    @Shadow
    @Final
    private GuiGraphics.ScissorStack scissorStack;
    @Unique
    private ScreenRectangle scissor;

    @Shadow
    protected abstract void applyScissor(@Nullable ScreenRectangle rectangle);

    @WrapOperation(method = "containsPointInScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics$ScissorStack;containsPoint(II)Z"))
    public boolean containsPoint(GuiGraphics.ScissorStack instance, int x, int y, Operation<Boolean> original) {
        if (this.scissor == null) {
            return original.call(instance, x, y);
        }
        return scissor.containsPoint(x, y);
    }

    @Inject(method = "applyScissor", at = @At("HEAD"), cancellable = true)
    public void ignoreIfExclusive(ScreenRectangle rectangle, CallbackInfo ci) {
        if (this.scissor != null && rectangle != this.scissor) {
            ci.cancel();
        }
    }

    @Override
    public @Nullable ScreenRectangle skyblockpv$popScissor() {
        var scissor = this.scissor;
        this.scissor = null;
        if (this.scissorStack instanceof ScissorStackAccessor accessor) {
            applyScissor(accessor.getScissorStack().peekLast());
        }
        return scissor;
    }

    @Override
    public void skyblockpv$pushScissor(@Nullable ScreenRectangle rectangle) {
        this.scissor = rectangle;
        this.applyScissor(rectangle);
    }

}
