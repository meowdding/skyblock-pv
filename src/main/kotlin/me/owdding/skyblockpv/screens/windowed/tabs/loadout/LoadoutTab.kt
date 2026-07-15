package me.owdding.skyblockpv.screens.windowed.tabs.loadout

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.layouts.withPadding
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.Inventory
import me.owdding.skyblockpv.api.data.InventoryData
import me.owdding.skyblockpv.api.data.InventoryData.ItemSet
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.SkillTreeType
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.screens.windowed.tabs.base.SimpleSkillTreeVisualizer
import me.owdding.skyblockpv.screens.windowed.tabs.mining.MiningSkillTreeScreen
import me.owdding.skyblockpv.utils.CarouselPageState
import me.owdding.skyblockpv.utils.CatppuccinColors
import me.owdding.skyblockpv.utils.ExtraWidgetRenderers
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.framed
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.displays.ExtraDisplays.withInventoryBackground
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
import kotlin.collections.chunked
import kotlin.math.max
import kotlin.sequences.mapTo

class LoadoutTab(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseWindowedPvScreen("Loadout", gameProfile, profile) {
    override val tab: PvTab = PvTab.LOADOUT

    var selected: Int = 1


    override fun create(bg: DisplayWidget) {
        val loadouts = profile.inventory?.loadouts?.savedLoadouts?.values?.sortedBy { it.id } ?: emptyList()
        val inventory = profile.inventory

        val loadout = profile.inventory?.loadouts

        context(loadout, inventory) {
            PvLayouts.frame(bg.width, bg.height) {

                val loadoutSelectorWidth = createLoadoutSelector(loadouts, bg.width / 5, bg.height).apply {
                    add {
                        alignHorizontallyLeft()
                        alignVerticallyMiddle()
                    }
                }.width

                val mainWidth = LayoutFactory.frame(bg.width - bg.width / 5, bg.height) {
                    mainView(loadouts.find { it.id == selected }, bg.width - bg.width / 3, bg.height).add()
                }.apply {
                    add {
                        alignHorizontallyRight()
                        alignVerticallyMiddle()
                    }
                }.width

                val width = bg.width - (loadoutSelectorWidth + mainWidth)

                val seperator = Widgets.renderable(WidgetRenderers.solid<BaseWidget>().withColor(CatppuccinColors.Mocha.surface0Color).withoutAlpha()).withSize(1, bg.height - 20)
                PvLayouts.horizontal {
                    spacer(loadoutSelectorWidth + width)
                    widget(seperator)
                }.add {
                    alignHorizontallyLeft()
                    alignVerticallyMiddle()
                }
            }.applyLayout(bg.x, bg.y)
        }
    }

    fun getEntry(map: Map<Int, ItemSet>?, id: Int?, equippedSlotId: Int?, equipped: Inventory?): List<ItemStack>? {
        if (id == equippedSlotId) {
            return equipped
        }

        return map?.values?.find { it.id == id }?.getStacks()
    }

    context(loadout: InventoryData.LoadoutData?, inventory: InventoryData?)
    fun createLoadoutSelector(loadouts: Collection<InventoryData.SavedLoadout>, width: Int, height: Int) =
        PvWidgets.label(
            "Loadouts",
            LayoutFactory.frame {
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
            },
        ).framed(width, height)

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
            return Items.DYE.red.defaultInstance
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
            return Items.DYE.gray.defaultInstance
        }


        return Items.DYE.green.defaultInstance
    }

    context(loadouts: InventoryData.LoadoutData?, inventory: InventoryData?)
    fun mainView(loadout: InventoryData.SavedLoadout?, width: Int, height: Int): LayoutElement = LayoutFactory.frame(width, height) {

        val armor = getEntry(loadouts?.armorSets, loadout?.armorSetId, loadouts?.equippedArmorSet, inventory?.armorItems?.reversed())
        val equipment = getEntry(loadouts?.equipmentSets, loadout?.equipmentSlotId, loadouts?.equippedEquipmentSet, inventory?.equipmentItems)

        val pet = profile.pets.find { it.uniqueId == loadout?.pet }

        horizontal(alignment = MIDDLE, spacing = 5) {
            val middle = LayoutFactory.vertical(alignment = MIDDLE, spacing = 5) {
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
            }

            val mining = SimpleSkillTreeVisualizer(
                loadout?.miningCoreSelectedSlot?.let { profile.skillTrees?.select(SkillTreeType.MINING, it) } ?: profile.skillTrees?.selectedMining,
                SkillTreeType.MINING,
            ).createLayout(FrameLayout(width / 2 - middle.width / 2 - 10, height))

            val foraging = SimpleSkillTreeVisualizer(
                loadout?.foragingCoreSelectedSlot?.let { profile.skillTrees?.select(SkillTreeType.FORAGING, it) } ?: profile.skillTrees?.selectedForaging,
                SkillTreeType.FORAGING,
            ).createLayout(FrameLayout(width / 2 - middle.width / 2 - 10, height))


            PvWidgets.label("HOTM Loadout", mining).add()
            PvWidgets.label("Equipment", middle).add()
            PvWidgets.label("HOTF Loadout", foraging).add()

        }
    }
}
