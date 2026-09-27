package me.owdding.skyblockpv.screens.windowed.tabs.loadout

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.Inventory
import me.owdding.skyblockpv.api.data.InventoryData
import me.owdding.skyblockpv.api.data.InventoryData.ItemSet
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.SkillTreeType
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.SimpleSkillTreeVisualizer
import me.owdding.skyblockpv.utils.CatppuccinColors
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.components.PvWidgets.withLabel
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class LoadoutTab(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseWindowedPvScreen("Loadout", gameProfile, profile) {
    override val tab: PvTab = PvTab.LOADOUT

    var selected: Int = 1


    override fun create(bg: DisplayWidget) {
        val loadouts = profile.inventory?.loadouts?.savedLoadouts?.values?.sortedBy { it.id } ?: emptyList()
        val inventory = profile.inventory
        val loadout = profile.inventory?.loadouts

        context(loadout, inventory) {
            val loadoutSelector = createLoadoutSelector(loadouts, bg.height)

            val mainViewWidth = bg.width - loadoutSelector.width - 10
            val mainViewWidget = mainView(loadouts.find { it.id == selected }, mainViewWidth, bg.height)

            val separator = Widgets.renderable(
                WidgetRenderers.solid<BaseWidget>()
                    .withColor(CatppuccinColors.Mocha.surface0Color)
                    .withoutAlpha(),
            ).withSize(1, bg.height - 20)

            PvLayouts.horizontal(spacing = 5) {
                widget(loadoutSelector) { alignVerticallyMiddle() }
                widget(separator) { alignVerticallyMiddle() }
                widget(mainViewWidget) { alignVerticallyMiddle() }
            }.setPos(bg.x, bg.y).visitWidgets(this@LoadoutTab::addRenderableWidget)
        }
    }

    fun getEntry(map: Map<Int, ItemSet>?, id: Int?, equippedSlotId: Int?, equipped: Inventory?): List<ItemStack>? {
        if (id == equippedSlotId) {
            return equipped
        }

        return map?.values?.find { it.id == id }?.getStacks()
    }

    context(loadout: InventoryData.LoadoutData?, inventory: InventoryData?)
    fun createLoadoutSelector(loadouts: Collection<InventoryData.SavedLoadout>, height: Int): LayoutElement {
        val content = LayoutFactory.frame {
            val widget = LayoutFactory.vertical {
                loadouts.chunked(3).rightPad(9, emptyList()).forEachIndexed { row, it ->
                    LayoutFactory.horizontal {
                        it.rightPad(3, null).mapIndexed { column, entry ->
                            val index = column + row * 3
                            Displays.item(entry.getIcon(inventory)).withTooltip {
                                this.add(entry.getName(index))

                                fun MutableComponent.appendValue(value: String?) {
                                    if (value != null) {
                                        append(value, CatppuccinColors.Mocha.sapphire)
                                    } else {
                                        append("None", CatppuccinColors.Mocha.red)
                                    }
                                }

                                this.add {
                                    append("Id - ", CatppuccinColors.Mocha.text)
                                    append(entry?.id?.toString() ?: index.toString(), CatppuccinColors.Mocha.sapphire)
                                }

                                fun addEntries(name: String, id: Int?, list: List<ItemStack>?) {
                                    this.add {
                                        append("$name - ", CatppuccinColors.Mocha.text)
                                        appendValue(id?.toString())
                                    }

                                    if (id != null && !list.isNullOrEmpty()) {
                                        list.forEach {
                                            add(" - ") {
                                                this.color = CatppuccinColors.Mocha.text
                                                append(it.takeUnless { it.isEmpty }?.hoverName ?: Text.of("None", CatppuccinColors.Mocha.red))
                                            }
                                        }
                                        space()
                                    }
                                }

                                addEntries(
                                    "Armor Set",
                                    entry?.armorSetId,
                                    getEntry(
                                        loadout?.armorSets,
                                        entry?.armorSetId,
                                        loadout?.equippedArmorSet,
                                        inventory?.armorItems?.reversed(),
                                    ),
                                )

                                addEntries(
                                    "Equipment Set",
                                    entry?.equipmentSlotId,
                                    getEntry(
                                        loadout?.equipmentSets,
                                        entry?.equipmentSlotId,
                                        loadout?.equippedEquipmentSet,
                                        inventory?.equipmentItems,
                                    ),
                                )

                                add("Hotm Preset - ") {
                                    this.color = CatppuccinColors.Mocha.text
                                    appendValue(entry?.miningCoreSelectedSlot?.toString())
                                }

                                add("Hotf Preset - ") {
                                    this.color = CatppuccinColors.Mocha.text
                                    appendValue(entry?.foragingCoreSelectedSlot?.toString())
                                }
                            }.withPadding(2).asButtonLeft {
                                selected = entry?.id ?: (index + 1)
                                safelyRebuild()
                            }.add()
                        }
                    }.add()
                }
            }
            widget(ExtraDisplays.inventoryBackground(3, 9, Displays.empty(widget.width, widget.height).withPadding(2)).asWidget()) {
                alignHorizontallyCenter()
                alignVerticallyMiddle()
            }
            widget(widget) {
                alignHorizontallyCenter()
                alignVerticallyMiddle()
            }
        }

        val labelWidget = PvWidgets.label("Loadouts", content)

        return if (labelWidget.height > height) {
            labelWidget.asScrollable(labelWidget.width + 20, height)
        } else {
            labelWidget
        }
    }

    fun InventoryData.SavedLoadout?.getName(index: Int): MutableComponent = when {
        this == null -> Text.of("Template $index") {
            this.color = CatppuccinColors.Mocha.text
            append(" (Locked)", CatppuccinColors.Mocha.red)
        }

        this.isEmpty -> Text.of("Template $index") {
            this.color = CatppuccinColors.Mocha.text
            append(" (Empty)", CatppuccinColors.Mocha.red)
        }

        else -> this.name.asComponent {
            this.color = CatppuccinColors.Mocha.text
        }
    }

    context(loadouts: InventoryData.LoadoutData?)
    fun InventoryData.SavedLoadout?.getIcon(inventory: InventoryData?): ItemStack {
        if (this == null) {
            return Items.DYE.red().defaultInstance
        }

        fun getItem(items: List<ItemStack>?): ItemStack? {
            val first = items?.firstNotNullOfOrNull { it.takeUnless { it.isEmpty } }
            if (first != null) {
                return first
            }

            return null
        }

        getItem(
            getEntry(
                loadouts?.armorSets,
                armorSetId,
                loadouts?.equippedArmorSet,
                inventory?.armorItems?.reversed(),
            ),
        )?.let { return it }
        getItem(
            getEntry(
                loadouts?.equipmentSets,
                equipmentSlotId,
                loadouts?.equippedEquipmentSet,
                inventory?.equipmentItems,
            ),
        )?.let { return it }

        if (this.miningCoreSelectedSlot != null) {
            return SkullTextures.HOTM.skull
        }

        if (this.foragingCoreSelectedSlot != null) {
            return SkullTextures.HOTF.skull
        }

        if (this.pet != null) {
            profile.pets.find { it.uniqueId == this.pet }?.itemStack?.let {
                return it
            }
        }

        if (this.isEmpty) {
            return Items.DYE.gray().defaultInstance
        }


        return Items.DYE.green().defaultInstance
    }

    context(loadouts: InventoryData.LoadoutData?, inventory: InventoryData?)
    fun mainView(loadout: InventoryData.SavedLoadout?, width: Int, height: Int): LayoutElement {
        val armor = getEntry(loadouts?.armorSets, loadout?.armorSetId, loadouts?.equippedArmorSet, inventory?.armorItems?.reversed())
        val equipment = getEntry(loadouts?.equipmentSets, loadout?.equipmentSlotId, loadouts?.equippedEquipmentSet, inventory?.equipmentItems)

        val pet = profile.pets.find { it.uniqueId == loadout?.pet }

        val equipWidget = PvLayouts.vertical(alignment = MIDDLE, spacing = 5) {
            display(
                ExtraDisplays.inventoryBackground(
                    2, 4,
                    Displays.padding(
                        2,
                        listOf(
                            PvWidgets.orderedArmorDisplay(armor.orEmpty()),
                            PvWidgets.orderedEquipmentDisplay(equipment.orEmpty()),
                        ).toRow(),
                    ),
                ),
            )
            display(
                ExtraDisplays.inventorySlot(
                    (pet?.itemStack?.let {
                        Displays.item(it, showTooltip = true, customStackText = pet.level)
                    } ?: Displays.background(ThemeSupport.texture(SkyBlockPv.id("icon/slot/bone")), Displays.empty(16, 16))).withPadding(2),
                ),
            )
        }.withLabel("Equipment")

        val treeHeight = (height - 25).coerceAtLeast(100)

        val miningVisualizer = SimpleSkillTreeVisualizer(
            loadout?.miningCoreSelectedSlot?.let { profile.skillTrees?.select(SkillTreeType.MINING, it) } ?: profile.skillTrees?.selectedMining,
            SkillTreeType.MINING,
        )
        val foragingVisualizer = SimpleSkillTreeVisualizer(
            loadout?.foragingCoreSelectedSlot?.let { profile.skillTrees?.select(SkillTreeType.FORAGING, it) } ?: profile.skillTrees?.selectedForaging,
            SkillTreeType.FORAGING,
        )

        // Layout - All horizontally
        val treeWidth1 = ((width - equipWidget.width - 25) / 2).coerceAtLeast(0)

        val hotmWidget1 = miningVisualizer.createLayout(FrameLayout(treeWidth1, treeHeight)).withLabel("HOTM Loadout")
        val hotfWidget1 = foragingVisualizer.createLayout(FrameLayout(treeWidth1, treeHeight)).withLabel("HOTF Loadout")

        val totalLayout1Width = hotmWidget1.width + equipWidget.width + hotfWidget1.width + 10

        val content = if (totalLayout1Width <= width && treeWidth1 >= 90) {
            PvLayouts.horizontal(alignment = MIDDLE, spacing = 5) {
                widget(hotmWidget1)
                widget(equipWidget)
                widget(hotfWidget1)
            }
        } else {
            // Layout - Equipment Row + Trees Row
            val treeWidth2 = ((width - 20) / 2).coerceAtLeast(0)

            val hotmWidget2 = miningVisualizer.createLayout(FrameLayout(treeWidth2, treeHeight)).withLabel("HOTM Loadout")
            val hotfWidget2 = foragingVisualizer.createLayout(FrameLayout(treeWidth2, treeHeight)).withLabel("HOTF Loadout")

            val totalLayout2TreeWidth = hotmWidget2.width + hotfWidget2.width + 5

            if (totalLayout2TreeWidth <= width && treeWidth2 >= 90) {
                PvLayouts.vertical(alignment = MIDDLE, spacing = 10) {
                    widget(equipWidget) { alignHorizontallyCenter() }
                    widget(
                        PvLayouts.horizontal(alignment = MIDDLE, spacing = 5) {
                            widget(hotmWidget2)
                            widget(hotfWidget2)
                        },
                    ) { alignHorizontallyCenter() }
                }
            } else {
                // Layout - All Vertically
                val hotmWidget3 = miningVisualizer.createLayout(FrameLayout(width - 10, treeHeight)).withLabel("HOTM Loadout")
                val hotfWidget3 = foragingVisualizer.createLayout(FrameLayout(width - 10, treeHeight)).withLabel("HOTF Loadout")

                PvLayouts.vertical(alignment = MIDDLE, spacing = 10) {
                    widget(equipWidget) { alignHorizontallyCenter() }
                    widget(hotmWidget3) { alignHorizontallyCenter() }
                    widget(hotfWidget3) { alignHorizontallyCenter() }
                }
            }
        }

        val centeredContent = LayoutFactory.frame(width, content.height) {
            widget(content) {
                alignHorizontallyCenter()
            }
        }

        return if (centeredContent.height > height) {
            centeredContent.asScrollable(width + 20, height)
        } else {
            centeredContent
        }
    }
}
