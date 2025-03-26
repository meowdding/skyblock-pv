package tech.thatgravyboat.skyblockpv.utils.components

import earth.terrarium.olympus.client.components.Widgets
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicateHelper
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicates
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import tech.thatgravyboat.skyblockpv.utils.displays.toColumn
import tech.thatgravyboat.skyblockpv.utils.displays.toRow

object PvWidgets {

    fun label(title: String, element: LayoutElement, padding: Int = 0) = LayoutBuild.vertical {
        widget(getTitleWidget(title, element.width + padding))
        widget(getMainContentWidget(element, element.width + padding))
    }

    fun tools(
        profile: SkyBlockProfile,
        score: (ItemStack) -> Int,
        tools: List<String>,
        emptyIcon: String,
        maxAmount: Int = 4,
    ): LayoutElement {
        val toolsToDisplay = (ItemPredicateHelper.getItemsMatching(
            profile,
            ItemPredicates.AnySkyblockID(tools),
        )?.sortedByDescending(score) ?: emptyList()).distinctBy { it.getData(DataTypes.ID) }.take(maxAmount).toMutableList()

        while (toolsToDisplay.size < maxAmount) {
            toolsToDisplay.add(Items.AIR.defaultInstance)
        }

        val column = toolsToDisplay.map { tool ->
            Displays.item(tool, showTooltip = true).let { display ->
                // TODO: hover over empty slot
                display.takeUnless { tool.isEmpty } ?: Displays.background(SkyBlockPv.id(emptyIcon), display)
            }.let { Displays.padding(2, it) }
        }.toColumn()

        return Displays.background(
            SkyBlockPv.id("inventory/inventory-1x$maxAmount"),
            Displays.padding(2, column),
        ).asWidget()
    }

    fun armorAndEquipment(
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
            armorDisplay(armorAndEquipment),
            displayEquipment.toColumn(),
        ).toRow()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-2x4"),
            Displays.padding(2, armorEquipment),
        ).asWidget()
    }

    fun armorDisplay(list: List<ItemStack>) = Displays.column(
        *buildList {
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
        }.toTypedArray(),
    )

    fun getTitleWidget(title: String, width: Int, icon: ResourceLocation? = null): LayoutElement = Widgets.frame { compoundWidget ->
        compoundWidget.withContents { contents ->
            contents.addChild(Displays.background(SkyBlockPv.id("box/title"), width - 10, 20).asWidget())
            if (icon != null) contents.addChild(Displays.padding(0, width - 30, 0, 0, Displays.sprite(icon, 12, 12)).asWidget())
            contents.addChild(Widgets.text(title).centerHorizontally(width))
        }
        compoundWidget.withStretchToContentSize()
    }

    fun getMainContentWidget(content: LayoutElement, width: Int): LayoutElement = Widgets.frame { compoundWidget ->
        val contentWithSpacer = LayoutBuild.vertical {
            spacer(height = 7)
            widget(content)
            spacer(height = 7)
        }
        compoundWidget.withContents { contents ->
            contents.addChild(Displays.background(SkyBlockPv.id("box/box"), width - 10, contentWithSpacer.height).asWidget())
            contents.addChild(contentWithSpacer.centerHorizontally(width))
        }
        compoundWidget.withStretchToContentSize()
    }
}
