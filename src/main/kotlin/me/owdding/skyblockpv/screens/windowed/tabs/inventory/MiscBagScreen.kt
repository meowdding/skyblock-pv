package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.lazy.registryBoundLazy
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

val greenCandy by registryBoundLazy { SkyBlockItemsRepo.getItemStackOrDefault("GREEN_CANDY").apply { remove(DataComponents.LORE) } }
val carnivalMaskBag by registryBoundLazy { SkyBlockItemsRepo.getItemStackOrDefault("CARNIVAL_MASK_BAG").apply { remove(DataComponents.LORE) } }

class MiscBagScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen<Unit>(gameProfile, profile) {
    override fun getRawInventory(): Unit = Unit

    override val iconTooltip: Boolean = true
    override val itemStackSize = false

    override fun Unit.getInventories(): List<Display> = listOf(
        PvWidgets.createInventory(profile.inventory?.personalVault.orEmpty(36)),
        PvWidgets.createInventory(profile.inventory?.potionBag.orEmpty(45)),
        PvWidgets.createInventory(profile.inventory?.quiver.orEmpty(45)),
        PvWidgets.createInventory(profile.inventory?.candy.orEmpty(27)),
        PvWidgets.createInventory(profile.inventory?.carnivalMaskBag.orEmpty(9)),
    )

    override fun Unit.getIcons(): List<ItemStack> = listOf(
        SkullTextures.PERSONAL_VAULT.skull.withTooltip { add("Personal Vault", TextColor.GREEN) },
        Items.POTION.defaultInstance.withTooltip { add("Potion Bag", TextColor.GREEN) },
        Items.ARROW.defaultInstance.withTooltip { add("Quiver", TextColor.GREEN) },
        greenCandy.withTooltip { add("Candy Bag", TextColor.GREEN) },
        carnivalMaskBag.withTooltip { add("Carnival Mask Bag", TextColor.GREEN) },
    )
}
