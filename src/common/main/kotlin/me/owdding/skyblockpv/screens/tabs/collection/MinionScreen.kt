package me.owdding.skyblockpv.screens.tabs.collection

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.rightPad
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.MinionCodecs
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.components.CarouselWidget
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class MinionScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCollectionScreen(gameProfile, profile) {
    private var carousel: CarouselWidget? = null

    data class MinionContext(val type: String, val maxObtainedLevel: Int)

    fun getInventories(): List<Display> {
        val minions = profile.minions ?: emptyList()

        return MinionCodecs.categories.sortedBy { it.index }.map {
            ExtraDisplays.inventoryBackground(5, 4, createMinions(it, minions).withPadding(2))
        }
    }

    fun createMinions(category: MinionCodecs.MinionCategory, minions: List<String>): Display {
        return category.minions.map {
            MinionContext(
                it,
                minions.find { minion -> minion.startsWith(it) }?.filter { it.isDigit() }?.toIntOrNull() ?: -1,
            )
        }.map(::createMinion)
            .map { it.withPadding(2) }
            .rightPad(20, Displays.empty(20, 20))
            .chunked(5)
            .asTable()
    }

    fun createMinion(context: MinionContext): Display {
        val minion = RepoItemsAPI.getItem("${context.type}_GENERATOR_${context.maxObtainedLevel.coerceAtLeast(1)}")
        val maxTier = MinionCodecs.miscData.getMax(context.type)

        val stackSize = when (context.maxObtainedLevel) {
            -1 -> null
            maxTier -> Text.of("${context.maxObtainedLevel}") { color = PvColors.GREEN }
            else -> Text.of("${context.maxObtainedLevel}") { color = PvColors.YELLOW }
        }

        return Displays.item(minion.takeUnless { context.maxObtainedLevel == -1 } ?: Items.GRAY_DYE.defaultInstance, customStackText = stackSize).withTooltip {
            add(minion.cleanName.substringBeforeLast(" ")) {
                this.color = PvColors.BLUE
            }

            add("Max Tier: ") {
                this.color = PvColors.GRAY
                append(maxTier) {
                    this.color = PvColors.YELLOW
                }
            }
            add("Highest Unlocked: ") {
                this.color = PvColors.GRAY
                append(context.maxObtainedLevel.takeUnless { it == -1 }?.let { "$it" } ?: "None") {
                    this.color = when (context.maxObtainedLevel) {
                        maxTier -> PvColors.GREEN
                        -1 -> PvColors.RED
                        else -> PvColors.YELLOW
                    }
                }
            }
        }
    }

    override fun getLayout(bg: DisplayWidget) = PvLayouts.vertical {
        val inventories = getInventories()
        val icons = MinionCodecs.categories.sortedBy { it.index }

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val buttonContainer = carousel!!.getIcons {
            List(inventories.size) { index ->
                val icon = icons[index]

                Displays.item(icon.display.value).withTooltip {
                    add(icon.title)
                }
            }
        }



        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))
    }
}
