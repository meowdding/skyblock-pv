package me.owdding.skyblockpv.feature.networth

import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils.filterNotAir
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.LowestBinAPI
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.Pricing
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

enum class NetworthCategory(val source: NetworthSource, formatted: String? = null) {
    CURRENCY(CurrencySource, "Purse/Bank"),
    INVENTORY(InventorySource),
    ARMOR(ArmorSource),
    ENDERCHEST(EnderchestSource),
    BACKPACKS(BackpacksSource),
    SACKS(SacksSource),
    PETS(PetsSource),
    LOADOUT(LoadoutSource),
    EQUIPMENT(EquipmentSource),
    TALISMAN_BAG(TalismanBagSource),
    FISHING_BAG(FishingBagSource),
    QUIVER_BAG(QuiverBagSource),
    PERSONAL_VAULT(PersonalVaultSource),
    ;

    val formatted = formatted ?: toFormattedName()
    override fun toString() = formatted
}

interface NetworthSource {
    fun getTotalValue(profile: SkyBlockProfile): Long = getItemValues(profile).values.sum()
    fun getItemValues(profile: SkyBlockProfile): Map<String, Long>

    fun MutableMap<String, Long>.putIfNotNull(key: String, value: Long?) {
        if (value != null) {
            this[key] = value
        }
    }
}

interface ItemListNetworthSource : NetworthSource {
    override fun getItemValues(profile: SkyBlockProfile): Map<String, Long> = buildMap {
        getItems(profile)?.filterNotAir()?.forEach {
            put(it.cleanName, it.getItemValue().price)
        }
    }

    fun getItems(profile: SkyBlockProfile): List<ItemStack>?
}

object CurrencySource : NetworthSource {
    override fun getItemValues(profile: SkyBlockProfile) = buildMap {
        putIfNotNull("Purse", profile.currency?.purse)
        putIfNotNull("Solo Bank", profile.bank?.soloBank)
        putIfNotNull("Profile Bank", profile.bank?.profileBank)
    }
}

object InventorySource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.inventoryItems
}

object ArmorSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.armorItems
}

object EnderchestSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.enderChestPages?.flatten()
}

object BackpacksSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.backpacks?.flatten()
}

object SacksSource : NetworthSource {
    override fun getItemValues(profile: SkyBlockProfile): Map<String, Long> = buildMap {
        profile.inventory?.sacks?.forEach { (id, amount) ->
            val unitPrice = Pricing.getPrice(id)
            if (unitPrice > 0) {
                val displayName = SkyBlockItemsRepo.getLazyItemStack(id)?.getDisplayName() ?: Text.of("Unknown Item")
                this[displayName.stripped] = unitPrice * amount
            }
        }
    }
}

object PetsSource : NetworthSource {
    override fun getItemValues(profile: SkyBlockProfile) = buildMap {
        profile.pets.forEach {
            val pet = LowestBinAPI.getLowestPrice("pet:${it.type}:${it.rarity}") ?: 0L
            val item = Pricing.getPrice(it.heldItem)
            val skin = Pricing.getPrice(it.skin)
            this[it.itemStack.cleanName] = pet + item + skin
        }
    }
}

object LoadoutSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? {
        val loadout = profile.inventory?.loadouts ?: return null
        return loadout.equipmentSets.flatMap { it.value.getStacks() } + loadout.armorSets.flatMap { it.value.getStacks() }
    }
}

object EquipmentSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.equipmentItems
}

object TalismanBagSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.talismans?.flatten()
}

object FishingBagSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.fishingBag
}

object QuiverBagSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.quiver
}

object PersonalVaultSource : ItemListNetworthSource {
    override fun getItems(profile: SkyBlockProfile): List<ItemStack>? = profile.inventory?.personalVault
}
