package me.owdding.skyblockpv.screens.windowed.tabs.museum

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.transpose
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.museum.MuseumArmor
import me.owdding.skyblockpv.data.museum.MuseumData
import me.owdding.skyblockpv.data.museum.RepoMuseumData
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.DropdownContext
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.displays.withDropdown
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class MuseumArmorScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseMuseumScreen(gameProfile, profile) {

    override fun getLayout(bg: DisplayWidget): Layout {
        return PvLayouts.frame {

            val chunked = RepoMuseumData.armor.map {
                loaded(
                    Displays.item(
                        Items.ORANGE_DYE.defaultInstance.withTooltip {
                            add("Loading...") {
                                this.color = PvColors.GOLD
                            }
                        },
                        showTooltip = true,
                    ),
                    Displays.item(
                        Items.BEDROCK.defaultInstance.withTooltip {
                            add("Error!") {
                                this.color = PvColors.RED
                            }
                        },
                        showTooltip = true,
                    ),
                ) { data -> createArmor(it, data) }
            }.map { Displays.padding(2, it) }.chunked(((bg.width - 20) / 20).coerceAtMost(15))

            val armors = ExtraDisplays.inventoryBackground(
                chunked.firstOrNull()?.size ?: 0, chunked.size,
                Displays.padding(2, chunked.map { it.toRow() }.toColumn()),
            )

            val actualWidget = PvLayouts.frame { display(armors) }.asScrollable(bg.width - 15, bg.height)
            widget(actualWidget)


            display(
                Displays.row(
                    ExtraDisplays.dropdownOverlay(
                        Displays.empty(armors.getWidth(), (actualWidget.height - 20).coerceAtMost(armors.getHeight())),
                        0x7F000000,
                        dropdownContext,
                    ),
                    Displays.empty(18),
                ),
            )
        }
    }

    private val dropdownContext = DropdownContext()

    private fun createArmor(museumArmor: MuseumArmor, data: MuseumData): Display {
        return data.items.find { it.id == museumArmor.id }?.let {
            val table = it.stacks.map { Displays.item(it.value, showTooltip = true).withPadding(2) }.chunked(4)
            val dropdown = table.transpose().asTable().let { display ->
                ExtraDisplays.inventoryBackground(table.size, it.stacks.size.coerceAtMost(4), display.withPadding(2))
                    .withPadding(top = -4, left = -4)
            }

            Displays.item(it.stacks.first().value, showTooltip = false).withDropdown(dropdown, dropdownContext)
        } ?: Displays.item(Items.GRAY_DYE).withDropdown(
            Displays.item(
                Items.GRAY_DYE.defaultInstance.withTooltip {
                    add("Missing Armor") { this.color = PvColors.RED }
                    museumArmor.armorIds.map { RepoItemsAPI.getItem(it) }.sortedByDescending { sortAmor(it) }.forEach {
                        add(it.hoverName)
                    }
                },
                showTooltip = true,
            ).withPadding(2).let { ExtraDisplays.inventorySlot(it.withPadding(2)) }.withPadding(top = -4, left = -4),
            dropdownContext,
        )
    }

    fun sortAmor(itemStack: ItemStack): Int {
        val skyBlockCategory = itemStack.getData(DataTypes.CATEGORY) ?: return -1
        return when (skyBlockCategory.name) {
            "helmet", "hat", "mask" -> return 8
            "chestplate" -> return 7
            "leggings" -> return 6
            "boots" -> return 5
            "necklace" -> return 4
            "cloak" -> return 3
            "belt" -> 2
            "bracelet", "gloves" -> return 1
            else -> -1
        }
    }

}
