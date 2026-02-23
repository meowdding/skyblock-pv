package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.Inventory
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

val greenCandy by RepoItemsAPI.getItemLazy("GREEN_CANDY")
val carnivalMaskBag by RepoItemsAPI.getItemLazy("CARNIVAL_MASK_BAG")

class MiscBagScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen<Unit>(gameProfile, profile) {
    override fun getRawInventory(): Unit = Unit

    override fun Unit.getInventories(): List<Display> = listOf(
        PvWidgets.createInventory(profile.inventory?.personalVault.orEmpty(36)),
        PvWidgets.createInventory(profile.inventory?.potionBag.orEmpty(45)),
        PvWidgets.createInventory(profile.inventory?.quiver.orEmpty(45)),
        PvWidgets.createInventory(profile.inventory?.fishingBag.orEmpty(45)),
        PvWidgets.createInventory(profile.inventory?.candy.orEmpty(27)),
        PvWidgets.createInventory(profile.inventory?.carnivalMaskBag.orEmpty(9)),
    )

    override fun Unit.getIcons(): List<ItemStack> = listOf(
        SkullTextures.PERSONAL_VAULT.skull,
        Items.POTION.defaultInstance,
        Items.ARROW.defaultInstance,
        Items.FISHING_ROD.defaultInstance,
        greenCandy,
        carnivalMaskBag,
    )

    override val itemStackSize = false
}
