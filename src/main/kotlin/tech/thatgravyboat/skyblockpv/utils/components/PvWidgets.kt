package tech.thatgravyboat.skyblockpv.utils.components

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import tech.thatgravyboat.lib.builder.LayoutBuild
import tech.thatgravyboat.lib.displays.*
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicateHelper
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicates
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.displays.ExtraDisplays

object PvWidgets {

    fun iconNumberElement(icon: ItemStack, text: Component) = listOf(
        Displays.item(icon, 12, 12),
        Displays.padding(0, 0, 2, 0, Displays.text(text, shadow = false)),
    ).toRow(1).let { Displays.background(SkyBlockPv.id("box/rounded_box_thin"), Displays.padding(2, it)) }

    fun iconNumberElement(icon: ItemLike, text: Component) = iconNumberElement(icon.asItem().defaultInstance, text)

    fun label(title: String, element: LayoutElement, padding: Int = 0) = LayoutBuild.vertical {
        widget(getTitleWidget(title, element.width + padding + 20))
        widget(getMainContentWidget(element, element.width + padding + 20))
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

        return ExtraDisplays.inventoryBackground(
            maxAmount, Orientation.VERTICAL,
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
        return ExtraDisplays.inventoryBackground(
            2, 4,
            Displays.padding(2, armorEquipment),
        ).asWidget()
    }

    private val ARMOR_BACKGROUND_IDS = listOf<ResourceLocation>(
        ResourceLocation.withDefaultNamespace("container/slot/helmet"),
        ResourceLocation.withDefaultNamespace("container/slot/chestplate"),
        ResourceLocation.withDefaultNamespace("container/slot/leggings"),
        ResourceLocation.withDefaultNamespace("container/slot/boots"),
    )

    fun orderedArmorDisplay(list: List<ItemStack>): Display {
        val armor = list.take(4).toMutableList().rightPad(4, ItemStack.EMPTY)

        return armor.mapIndexed { index, item ->
            if (item.isEmpty) {
                Displays.background(ARMOR_BACKGROUND_IDS[index], 16, 16)
            } else {
                Displays.item(item, showTooltip = true)
            }
        }.map { Displays.padding(2, it) }.toColumn()
    }

    private val EQUIPMENT_BACKGROUND_IDS = listOf<ResourceLocation>(
        SkyBlockPv.id("icon/slot/necklace"),
        SkyBlockPv.id("icon/slot/cloak"),
        SkyBlockPv.id("icon/slot/belt"),
        SkyBlockPv.id("icon/slot/glove"),
    )

    fun orderedEquipmentDisplay(list: List<ItemStack>): Display {
        val armor = list.take(4).toMutableList().rightPad(4, ItemStack.EMPTY)

        return armor.mapIndexed { index, item ->
            if (item.isEmpty) {
                Displays.background(EQUIPMENT_BACKGROUND_IDS[index], 16, 16)
            } else {
                Displays.item(item, showTooltip = true)
            }
        }.map { Displays.padding(2, it) }.toColumn()
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

    fun createInventory(items: List<ItemStack>): Display {
        val itemDisplays = items.chunked(9).map { chunk ->
            val updatedChunk = chunk + List(9 - chunk.size) { ItemStack.EMPTY }
            updatedChunk.map { item ->
                Displays.padding(2, Displays.item(item, showTooltip = true, showStackSize = true))
            }
        }
        return ExtraDisplays.inventoryBackground(
            9, itemDisplays.size,
            Displays.padding(2, itemDisplays.asTable()),
        )
    }
}
