package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.owdding.skyblockpv.accessor.WidgetSpritesAccessor;
import me.owdding.skyblockpv.utils.theme.ThemeSupport;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WidgetSprites.class)
public class WidgetSpritesMixin implements WidgetSpritesAccessor {

    @Unique
    private boolean skyblockpv$enableThemeSupport = false;

    @Override
    public void skyblockpv$enableThemeSupport() {
        skyblockpv$enableThemeSupport = true;
    }

    @ModifyReturnValue(method = {"disabled", "disabledFocused", "enabled", "enabledFocused", "get"}, at = @At("RETURN"))
    public ResourceLocation get(ResourceLocation resourceLocation) {
        if (skyblockpv$enableThemeSupport) {
            return ThemeSupport.INSTANCE.texture(resourceLocation);
        }

        return resourceLocation;
    }
}
