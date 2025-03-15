package tech.thatgravyboat.skyblockpv.mixin;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(I18n.class)
public interface I18nAccessor {

    @Accessor("language")
    static Language getLanguage() {
        throw new AssertionError();
    }
}
