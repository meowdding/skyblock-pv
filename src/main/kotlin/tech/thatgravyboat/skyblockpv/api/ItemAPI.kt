package tech.thatgravyboat.skyblockpv.api

import com.mojang.serialization.JsonOps
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.utils.createSkull

object ItemAPI {

    private val itemCache: MutableMap<String, ItemStack> = mutableMapOf()
    private val petCache: MutableMap<PetData, ItemStack> = mutableMapOf()

    init {
        RepoAPI.setup()
    }

    fun getItem(id: String): ItemStack = itemCache.getOrPut(id) {
        val data = RepoAPI.items().getItem(id)
        ItemStack.CODEC.parse(JsonOps.INSTANCE, data).result().orElse(null) ?: fallback(id)
    }

    fun getPet(petData: PetData): ItemStack = petCache.getOrPut(petData) {
        val data = RepoAPI.pets().getPet(petData.id)
        val pet = data.tiers[petData.rarity.name]
        val hasSkin = petData.skin != null
        val skin = petData.skin?.let { getItem("PET_SKIN_$it") }

        pet?.let {
            (skin.takeIf { hasSkin } ?: createSkull(it.texture)).apply {
                val name = Text.join(data.name, if (hasSkin) " (Skin)" else CommonText.EMPTY)
                set(DataComponents.CUSTOM_NAME, name)
                set(DataComponents.LORE, ItemLore(it.lore.map(Text::of)))
            }
        } ?: fallback(petData.id)
    }

    fun getPet(id: String, rarity: SkyBlockRarity, level: Int = 1, skin: String? = null): ItemStack = getPet(PetData(id, rarity, level, skin))

    private fun fallback(id: String): ItemStack = ItemStack(Items.BARRIER).apply { this.set(DataComponents.ITEM_NAME, Text.of(id)) }

    data class PetData(
        val id: String,
        val rarity: SkyBlockRarity,
        val level: Int,
        val skin: String?,
    )
}
