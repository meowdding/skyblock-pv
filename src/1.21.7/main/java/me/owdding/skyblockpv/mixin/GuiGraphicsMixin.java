package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyblockpv.mixin.accessors.ScissorStackAccessor;
import me.owdding.skyblockpv.utils.accessors.GuiGraphicsAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphics.class)
abstract public class GuiGraphicsMixin implements GuiGraphicsAccessor {
    @Shadow
    @Final
    public GuiGraphics.ScissorStack scissorStack;
    @Unique
    private ScreenRectangle scissor;

    @WrapOperation(method = "containsPointInScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics$ScissorStack;containsPoint(II)Z"))
    public boolean containsPoint(GuiGraphics.ScissorStack instance, int x, int y, Operation<Boolean> original) {
        if (this.scissor == null) {
            return original.call(instance, x, y);
        }
        return true;
    }

    @WrapOperation(method = "enableScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics$ScissorStack;push(Lnet/minecraft/client/gui/navigation/ScreenRectangle;)Lnet/minecraft/client/gui/navigation/ScreenRectangle;"))
    public ScreenRectangle ignoreIfExclusive(GuiGraphics.ScissorStack instance, ScreenRectangle scissor, Operation<ScreenRectangle> original) {
        if (this.scissor == null || scissor == this.scissor) {
            return original.call(instance, scissor);
        }
        return null;
    }

    @Override
    public @Nullable ScreenRectangle skyblockpv$popScissor() {
        var scissor = this.scissor;
        this.scissor = null;
        this.scissorStack.pop();
        return scissor;
    }

    @Override
    public void skyblockpv$pushScissor(@NotNull ScreenRectangle rectangle) {
        this.scissor = rectangle;
        if (this.scissorStack instanceof ScissorStackAccessor accessor) {
            accessor.getScissorStack().addLast(rectangle);
        }
    }

}
