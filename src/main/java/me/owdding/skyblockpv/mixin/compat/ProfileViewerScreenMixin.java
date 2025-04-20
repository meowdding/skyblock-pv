package me.owdding.skyblockpv.mixin.compat;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModLoaded("skyblocker")
@Mixin(targets = "de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen")
public class ProfileViewerScreenMixin {
    @WrapWithCondition(
        method = "lambda$initClass$15",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/CommandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;",
            ordinal = 0
        )
    )
    private static <S> boolean wrapCommand(CommandDispatcher<S> instance, LiteralArgumentBuilder<S> command) {
        return false; // Add a config option here if you so desire
    }
}
