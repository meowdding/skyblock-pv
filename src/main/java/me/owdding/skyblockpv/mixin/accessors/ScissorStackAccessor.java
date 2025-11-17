package me.owdding.skyblockpv.mixin.accessors;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;

@Mixin(GuiGraphics.ScissorStack.class)
public interface ScissorStackAccessor {

    @Accessor("stack")
    Deque<ScreenRectangle> getScissorStack();

}
