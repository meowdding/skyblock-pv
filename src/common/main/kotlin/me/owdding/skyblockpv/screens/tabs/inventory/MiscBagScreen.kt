package me.owdding.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

val greenCandy by RepoItemsAPI.getItemLazy("GREEN_CANDY")
val carnivalMaskBag by RepoItemsAPI.getItemLazy("CARNIVAL_MASK_BAG")

class MiscBagScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    override fun getInventories(): List<Display> = listOf(
        PvWidgets.createInventory(profile.inventory?.personalVault?.inventory.orEmpty(36)),
        PvWidgets.createInventory(profile.inventory?.potionBag?.inventory.orEmpty(45)),
        PvWidgets.createInventory(profile.inventory?.quiver?.inventory.orEmpty(45)),
        PvWidgets.createInventory(profile.inventory?.fishingBag?.inventory.orEmpty(45)),
        PvWidgets.createInventory(profile.inventory?.candy?.inventory.orEmpty(27)),
        PvWidgets.createInventory(profile.inventory?.carnivalMaskBag?.inventory.orEmpty(9)),
    )

    override fun getIcons(): List<ItemStack> = listOf(
        SkullTextures.PERSONAL_VAULT.skull,
        Items.POTION.defaultInstance,
        Items.ARROW.defaultInstance,
        Items.FISHING_ROD.defaultInstance,
        greenCandy,
        carnivalMaskBag,
    )

    override val itemStackSize = false
}
