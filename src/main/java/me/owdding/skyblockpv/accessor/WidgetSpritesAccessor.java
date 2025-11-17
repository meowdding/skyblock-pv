package me.owdding.skyblockpv.accessor;

import net.minecraft.client.gui.components.WidgetSprites;

public interface WidgetSpritesAccessor {

    static void withThemeSupport(WidgetSprites widgetSprites) {
        //noinspection ConstantValue
        if (((Object) widgetSprites) instanceof WidgetSpritesAccessor accessor) {
            accessor.skyblockpv$enableThemeSupport();
        }
    }

    void skyblockpv$enableThemeSupport();

}
