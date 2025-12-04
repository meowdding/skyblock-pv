package me.owdding.skyblockpv.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.CommandDispatcher;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.commands.CommandBuildContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@IfModLoaded("skyblocker")
@Mixin(targets = "de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen", remap = false)
public class ProfileViewerScreenMixin {
    @WrapMethod(
        method = "lambda$initClass$15",
        require = 0
    )
    private static void wrapCommand(CommandDispatcher<?> dispatcher, CommandBuildContext registryAccess, Operation<Void> original) {
        //original.call(dispatcher, registryAccess);
    }
}
