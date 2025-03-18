package tech.thatgravyboat.skyblockpv.api

import com.mojang.serialization.JsonOps
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import tech.thatgravyboat.skyblockpv.utils.createSkull

object ItemAPI {

    private val itemCache: MutableMap<String, ItemStack> = mutableMapOf()
    private val petCache: MutableMap<PetData, ItemStack> = mutableMapOf()

    init {
        RepoAPI.setup()
    }

    fun getItem(id: String): ItemStack = itemCache.getOrPut(id) {
        val data = RepoAPI.items().getItem(id)
        ItemStack.CODEC.parse(JsonOps.INSTANCE, data).result().orElse(null) ?: fallbackItem(id)
    }

    fun getPet(petData: PetData): ItemStack = petCache.getOrPut(petData) {
        val data = RepoAPI.pets().getPet(petData.id)
        val pet = data.tiers[petData.rarity.name]
        val hasSkin = petData.skin != null
        val skin = petData.skin?.let { getItem("PET_SKIN_$it").copy() }

        pet?.let {
            (skin.takeIf { hasSkin } ?: createSkull(it.texture)).apply {
                val name = Text.join(
                    Text.of("[Lvl ${petData.level}] ").withColor(TextColor.GRAY),
                    Text.of(data.name).withColor(petData.rarity.color),
                    if (hasSkin) Text.of(" ✦").withColor(TextColor.LIGHT_PURPLE) else null,
                )
                name.italic = false
                val lore = pet.getFormattedLore(petData.level)
                set(DataComponents.CUSTOM_NAME, name)
                set(DataComponents.LORE, ItemLore(lore.map(Text::of)))
            }
        } ?: fallbackItem(petData.id)
    }

    fun getPet(id: String, rarity: SkyBlockRarity, level: Int = 1, skin: String? = null): ItemStack = getPet(PetData(id, rarity, level, skin))

    fun fallbackItem(id: String): ItemStack = ItemStack(Items.BARRIER).apply { this.set(DataComponents.ITEM_NAME, Text.of(id)) }

    data class PetData(
        val id: String,
        val rarity: SkyBlockRarity,
        val level: Int,
        val skin: String?,
    )
}
