package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyblockpv.config.Config;
import me.owdding.skyblockpv.screens.PvTab;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI;
import tech.thatgravyboat.skyblockapi.helpers.McClient;

import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {

    @Shadow
    @Final
    private static Component STATS;

    @Unique
    @Final
    private static Component sbpv$stats = Component.literal("Open Pv");

    @Unique
    @Final
    private static Component sbpv$statsHover = Component.literal("Open SkyBlock Profile Viewer\n")
        .append(Component.literal("Disable in the config.").withColor(TextColor.GRAY).withStyle(ChatFormatting.ITALIC));

    @WrapOperation(
        method = "createPauseMenu",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;"
        )
    )
    public Button overrideStatisticsButton(PauseScreen instance, Component message, Supplier<Screen> newScreen, Operation<Button> original) {
        if (message == STATS && Config.INSTANCE.getReplaceStatisticsButton() && LocationAPI.INSTANCE.isOnSkyBlock()) {
            var button = original.call(instance, sbpv$stats, (Supplier<Screen>) () -> PvTab.MAIN.create(McClient.INSTANCE.getSelf().getGameProfile(), null));
            button.setTooltip(Tooltip.create(sbpv$statsHover));
            return button;
        }
        return original.call(instance, message, newScreen);
    }

}
