package tech.thatgravyboat.skyblockpv.utils

import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicateHelper
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicates
import tech.thatgravyboat.skyblockpv.utils.displays.*

object GearUtils {

    fun getArmorAndEquipment(
        profile: SkyBlockProfile,
        score: (ItemStack) -> Int,
        necklaces: List<String>,
        cloaks: List<String>,
        belts: List<String>,
        gloves: List<String>,
        armor: List<String>,
    ): LayoutElement {
        val armorAndEquipment = ItemPredicateHelper.getItemsMatching(
            profile,
            ItemPredicates.AnySkyblockID(armor).or(ItemPredicates.AnySkyblockID(listOf(cloaks, necklaces, belts, gloves).flatten())),
        )?.sortedBy(score)?.reversed() ?: emptyList()

        val displayArmor = getDisplayArmor(armorAndEquipment)

        val displayEquipment = buildList {
            fun addEquipment(list: List<String>, name: String) {
                val item = armorAndEquipment.firstOrNull { it.getData(DataTypes.ID)?.let { id -> list.contains(id) } == true } ?: ItemStack.EMPTY
                val display = if (item.isEmpty) {
                    Displays.background(SkyBlockPv.id("icon/slot/${name.lowercase()}"), Displays.empty(16, 16))
                } else {
                    Displays.item(item, showTooltip = true)
                }

                add(Displays.padding(2, display))
            }

            addEquipment(necklaces, "necklace")
            addEquipment(cloaks, "cloak")
            addEquipment(belts, "belt")
            addEquipment(gloves, "glove")
        }

        val armorEquipment = listOf(
            displayArmor.toColumn(),
            displayEquipment.toColumn(),
        ).toRow()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-2x4"),
            Displays.padding(2, armorEquipment),
        ).centerIn(-1, -1).asWidget()
    }

    fun getDisplayArmor(list: List<ItemStack>) = buildList {
        fun addArmor(type: String) {
            val itemStack = list.firstOrNull { it.getData(DataTypes.ID)?.contains(type) != false } ?: ItemStack.EMPTY
            Displays.padding(
                2,
                Displays.item(itemStack, showTooltip = true).let {
                    it.takeUnless { itemStack.isEmpty } ?: Displays.background(
                        ResourceLocation.parse("container/slot/${type.lowercase()}"),
                        it,
                    )
                },
            ).let { add(it) }
        }

        addArmor("HELMET")
        addArmor("CHESTPLATE")
        addArmor("LEGGINGS")
        addArmor("BOOTS")
    }

}
