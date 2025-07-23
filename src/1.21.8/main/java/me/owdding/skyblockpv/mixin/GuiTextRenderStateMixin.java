package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.owdding.skyblockpv.MixinHelper;
import me.owdding.skyblockpv.accessor.FontPipelineHolder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiTextRenderState.class)
public class GuiTextRenderStateMixin {

    @WrapMethod(method = "ensurePrepared")
    private Font.PreparedText ensurePrepared(Operation<Font.PreparedText> original) {
        var holder = FontPipelineHolder.getHolder(this);
        if (holder != null) {
            MixinHelper.FONT_PIPELINE.set(holder.skyblockpv$getPipeline());
            var preparedText = original.call();
            MixinHelper.FONT_PIPELINE.remove();
            return preparedText;
        }
        return original.call();
    }
}
