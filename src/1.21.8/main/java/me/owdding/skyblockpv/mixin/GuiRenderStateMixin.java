package me.owdding.skyblockpv.mixin;

import me.owdding.skyblockpv.accessor.FontPipelineHolder;
import me.owdding.skyblockpv.utils.render.RenderUtils;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public class GuiRenderStateMixin {

    @Inject(method = "submitText", at = @At("HEAD"))
    private void prepareTextHead(@NotNull GuiTextRenderState state, CallbackInfo ci) {
        var shader = RenderUtils.INSTANCE.getTEXT_SHADER();
        if (shader == null) return;

        var stateHolder = (FontPipelineHolder) (Object) state;
        stateHolder.sbpv$setPipeline(shader.getPipeline());
    }
}
