package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.StaticGardenData
import tech.thatgravyboat.skyblockpv.data.asItemStack
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.displays.*
import kotlin.jvm.optionals.getOrNull

class FarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(): Layout {
        val data = gardenData?.getOrNull()

        return LayoutBuild.frame {
            horizontal {
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
                                it.name,
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
                )()

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

                createWidget(
                    "Plots",
                    LayoutBuild.frame {
                        val plots = map.map { it.reversed().map { Displays.padding(2, it) }.toColumn() }.toRow()
                        display(
                            Displays.background(
                                SkyBlockPv.id("inventory/inventory-5x5"),
                                Displays.padding(2, plots),
                            ),
                        )
                    },
                    padding = 20
                ).let { widget(it) }
            }
        }
    }

}

private operator fun <E> MutableList<MutableList<E>>.set(location: Vector2i, value: E) {
    this[location.x][location.y] = value
}
