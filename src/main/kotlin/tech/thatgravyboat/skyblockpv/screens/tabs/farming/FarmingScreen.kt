package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicateHelper
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicates
import tech.thatgravyboat.skyblockpv.data.FarmingEquipment
import tech.thatgravyboat.skyblockpv.data.Pet
import tech.thatgravyboat.skyblockpv.data.StaticGardenData
import tech.thatgravyboat.skyblockpv.data.asItemStack
import tech.thatgravyboat.skyblockpv.utils.GearUtils
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.displays.*

class FarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(): Layout {
        val profile = profile ?: return LayoutBuild.frame {}

        return LayoutBuild.frame {
            horizontal {
                widget(getPlots())
                widget(getTools())
                widget(getGear(profile))
            }
        }
    }

    private fun getPets(profile: SkyBlockProfile) = profile.pets.asSequence()
        .filter { FarmingEquipment.pets.contains(it.type) }
        .sortedWith(Comparator.comparingInt<Pet> { it.rarity.ordinal }.thenByDescending { pet -> pet.exp })
        .distinctBy { it.type }
        .mapTo(mutableListOf()) { Displays.item(it.itemStack, showTooltip = true, customStackText = it.level) }
        .rightPad(4, Displays.background(SkyBlockPv.id("icon/slot/bone"), Displays.empty(16, 16)))
        .map { Displays.padding(2, it) }
        .toColumn()
        .let { Displays.background(SkyBlockPv.id("inventory/inventory-1x4"), Displays.padding(2, it)) }

    private fun getGear(profile: SkyBlockProfile) = createWidget(
        "Gear",
        LayoutBuild.horizontal {
            widget(
                GearUtils.getArmorAndEquipment(
                    profile,
                    ::calculateEquipmentScore,
                    FarmingEquipment.necklaces,
                    FarmingEquipment.cloaks,
                    FarmingEquipment.belts,
                    FarmingEquipment.gloves,
                    FarmingEquipment.armor,
                ),
            )
            spacer(width = 5)
            display(getPets(profile))
            spacer(width = 5)
            widget(Displays.background(SkyBlockPv.id("inventory/inventory-1x1"), getVacuum(profile)).asWidget()) { alignVerticallyMiddle() }
        },
        padding = 20,
    )

    private fun getVacuum(profile: SkyBlockProfile): Display {
        val vacuums = ItemPredicateHelper.getItemsMatching(profile, ItemPredicates.AnySkyblockID(FarmingEquipment.vaccum)) ?: emptyList()

        val vacum = vacuums.sortedBy { it.getData(DataTypes.RARITY)?.ordinal ?: 0 }
            .reversed().firstOrNull() ?: return Displays.padding(4, Displays.background(SkyBlockPv.id("icon/slot/minecart"), Displays.empty(16, 16)))

        return Displays.padding(4, Displays.item(vacum, showTooltip = true))
    }

    fun getTools() = createWidget(
        "Tools",
        StaticGardenData.tools.map { tool ->
            val items = profile?.let { profile ->
                ItemPredicateHelper.getItemsMatching(profile, ItemPredicates.AnySkyblockID(tool.ids))
            } ?: listOf()

            if (items.isEmpty()) {
                return@map Displays.background(SkyBlockPv.id(tool.type.id), Displays.empty(16, 16)).withTooltip(tool.displayName)
            }

            val toMutableList = items.toMutableList()
            toMutableList.add(ItemStack.EMPTY)

            val first = toMutableList.sortedBy(::evaluateToolScore).reversed().first()
            Displays.item(first, showTooltip = true)
        }.map { Displays.padding(2, it) }.chunked(5).map { it.toRow() }.toColumn()
            .let { Displays.background(SkyBlockPv.id("inventory/inventory-5x2"), Displays.padding(2, it)) }
            .let { LayoutBuild.frame { display(it) } },
        padding = 20,
    )

    private fun calculateEquipmentScore(stack: ItemStack): Int {
        var score = 0

        score += stack.getData(DataTypes.RARITY)?.ordinal ?: 0

        score += stack.getData(DataTypes.ENCHANTMENTS)?.map { it.value }?.sum() ?: 0

        score += stack.getData(DataTypes.RARITY_UPGRADES) ?: 0

        score += stack.getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        return score
    }

    private fun evaluateToolScore(stack: ItemStack): Long {
        return stack.getData(DataTypes.CROPS_BROKEN) ?: stack.getData(DataTypes.RARITY)?.ordinal?.toLong() ?: -1L
    }

    fun getPlots() = createWidget(
        "Plots",
        LayoutBuild.frame {
            val data = gardenProfile?.getOrNull()
            val map = MutableList(5) { MutableList(5) { Displays.empty() } }

            StaticGardenData.plots.forEach {
                map[it.location] = Displays.tooltip(Displays.item(Items.BLACK_STAINED_GLASS_PANE.defaultInstance), it.getName())
            }

            fun fillMap(value: Display) {
                map.forEach {
                    it.fill(value)
                }
            }

            loading(
                {
                    data!!.unlockedPlots.forEach {
                        map[it.location] = Displays.tooltip(
                            Displays.item(
                                Items.GREEN_STAINED_GLASS_PANE.defaultInstance,
                            ),
                            it.getName().also { it.color = TextColor.GREEN },
                        )
                    }
                },
                {
                    fillMap(
                        Displays.tooltip(
                            Displays.item(Items.ORANGE_STAINED_GLASS_PANE.defaultInstance),
                            Text.of("Loading...") { this.color = TextColor.GOLD },
                        ),
                    )
                },
                {
                    fillMap(
                        Displays.tooltip(
                            Displays.item(Items.BEDROCK.defaultInstance),
                            Text.of("Error!") { this.color = TextColor.RED },
                        ),
                    )
                },
            ).invoke()

            map[2][2] = Displays.tooltip(
                Displays.item(
                    loading<ItemStack>(
                        data?.selectedBarnSkin?.item?.asItemStack() ?: ItemStack.EMPTY,
                        Items.BARRIER.defaultInstance,
                        Items.BEDROCK.defaultInstance,
                    ),
                ),
                loadingComponent(data?.selectedBarnSkin?.displayName ?: Component.empty()),
            )

            val plots = map.map { it.reversed().map { Displays.padding(2, it) }.toColumn() }.toRow()
            display(
                Displays.background(
                    SkyBlockPv.id("inventory/inventory-5x5"),
                    Displays.padding(2, plots),
                ),
            )
        },
        padding = 20,
    )

}

private operator fun <E> MutableList<MutableList<E>>.set(location: Vector2i, value: E) {
    this[location.x][location.y] = value
}
