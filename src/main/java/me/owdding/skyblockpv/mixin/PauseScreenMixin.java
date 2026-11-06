package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyblockpv.config.Config;
import me.owdding.skyblockpv.screens.PvTab;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI;
import tech.thatgravyboat.skyblockapi.helpers.McClient;

import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {

    @Shadow
    @Final
    private static Component STATS;

    @WrapOperation(
        method = "createPauseMenu",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;"
        )
    )
    public Button meow(PauseScreen instance, Component message, Supplier<Screen> newScreen, Operation<Button> original) {
        if (message == STATS && Config.INSTANCE.getReplaceStatisticsButton() && LocationAPI.INSTANCE.isOnSkyBlock()) {
            return original.call(instance, message, (Supplier<Screen>) () -> PvTab.MAIN.create(McClient.INSTANCE.getSelf().getGameProfile(), null));
        }
        return original.call(instance, message, newScreen);
    }

}
