package me.owdding.skyblockpv.mixin.accessors;

//~ if >= 26.1 'GuiGraphics' -> 'GuiGraphicsExtractor'
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;

//~ if >= 26.1 'GuiGraphics' -> 'GuiGraphicsExtractor'
@Mixin(GuiGraphicsExtractor.ScissorStack.class)
public interface ScissorStackAccessor {

    @Accessor("stack")
    Deque<ScreenRectangle> getScissorStack();

}
