package me.owdding.skyblockpv.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import earth.terrarium.olympus.client.components.textbox.autocomplete.AutocompleteScreen;
import earth.terrarium.olympus.client.ui.Overlay;
import me.owdding.skyblockpv.screens.BasePvScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AutocompleteScreen.class)
public class AutocompleteScreenMixin extends Overlay {

    protected AutocompleteScreenMixin(@Nullable Screen background) {
        super(background);
    }

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Learth/terrarium/olympus/client/components/textbox/autocomplete/AutocompleteScreen;renderBlurredBackground()V",
            remap = false
        ), method = "renderBackground", remap = false
    )
    public void removeBlurForPvScreens(AutocompleteScreen<?> instance, Operation<Void> original) {
        if (!(this.background instanceof BasePvScreen)) original.call(instance);
    }

    @ModifyExpressionValue(
        at = @At(value = "INVOKE", target = "Ljava/lang/String;isEmpty()Z", remap = false),
        method = "filter", remap = false
    )
    public boolean skipIsEmptyForPvScreens(boolean original) {
        if (this.background instanceof BasePvScreen) return false;
        return original;
    }
}
