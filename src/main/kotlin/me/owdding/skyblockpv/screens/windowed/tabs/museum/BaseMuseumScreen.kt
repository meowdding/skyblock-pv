package me.owdding.skyblockpv.screens.windowed.tabs.museum

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.toColumn
import me.owdding.lib.displays.toRow
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.api.MuseumAPI
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.museum.MuseumData
import me.owdding.skyblockpv.data.museum.RepoMuseumData
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedLoadingScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import me.owdding.skyblockpv.screens.windowed.tabs.base.FilterScreen
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.DropdownContext
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.max
import kotlin.math.min

abstract class BaseMuseumScreen<FilterType : Any, EntryType : Any>(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    AbstractCategorizedLoadingScreen<MuseumData>("MUSEUM", gameProfile, profile), FilterScreen<FilterType> {
    override val tab: PvTab = PvTab.MUSEUM

    override val api get() = MuseumAPI
    override val categories = MuseumCategory.entries

    abstract fun entries(): List<EntryType>
    abstract fun EntryType.toDisplay(data: MuseumData, dropdownContext: DropdownContext): List<Display>?

    override var query: String? = ""
    private val dropdownContext = DropdownContext()

    override fun getLayout(bg: DisplayWidget): Layout = createLayout(bg)

    override fun createLayout(width: Int, height: Int): Layout = PvLayouts.frame {
        val entries = entries().mapNotNull {
            loaded(
                listOf(
                    Displays.item(
                        Items.ORANGE_DYE.defaultInstance.withTooltip {
                            add("Loading...") {
                                this.color = PvColors.GOLD
                            }
                        },
                        showTooltip = true,
                    ),
                ),
                listOf(
                    Displays.item(
                        Items.BEDROCK.defaultInstance.withTooltip {
                            add("Error!") {
                                this.color = PvColors.RED
                            }
                        },
                        showTooltip = true,
                    ),
                ),
            ) { data ->
                it.toDisplay(data, dropdownContext)
            }
        }.flatten()

        if (entries.isEmpty()) {
            widget(Widgets.text("No Item match the input!").withColor(MinecraftColors.RED)) {
                alignHorizontallyCenter()
                alignVerticallyMiddle()
            }
            return@frame
        }

        val chunked = entries.map { Displays.padding(2, it) }.chunked(((width - 20) / 20).coerceAtMost(min(20, max(entries.size / 3, 3)))).toList()

        val items = ExtraDisplays.inventoryBackground(
            chunked.firstOrNull()?.size ?: 0, chunked.size,
            Displays.padding(2, chunked.map { it.toRow() }.toColumn()),
        )
        val actualWidget = PvLayouts.frame(items.getWidth(), items.getHeight()) { display(items) }
        widget(actualWidget)

        display(
            ExtraDisplays.dropdownOverlay(
                Displays.empty(items.getWidth(), items.getHeight()),
                0x7F000000,
                dropdownContext,
            ),
        )
    }
}

data object MiscMuseumCategory : Category {
    override fun create(
        gameProfile: GameProfile,
        profile: SkyBlockProfile?,
    ): BaseWindowedPvScreen = MiscMuseumScreen(gameProfile, profile)

    override val isSelected: Boolean
        get() = McScreen.self is MiscMuseumScreen

    override val icon: ItemStack = Items.CAKE.defaultInstance
}

data class MuseumCategory(val category: RepoMuseumData.MuseumCategory, override val icon: ItemStack, val name: String) : Category {
    companion object {
        val entries by lazy {
            RepoMuseumData.categories.map { (name, category) ->
                MuseumCategory(
                    category,
                    when (name) {
                        "combat" -> Items.STONE_SWORD.defaultInstance
                        "farming" -> Items.GOLDEN_HOE.defaultInstance
                        "mining" -> Items.STONE_PICKAXE.defaultInstance
                        "fishing" -> Items.FISHING_ROD.defaultInstance
                        "foraging" -> Items.JUNGLE_SAPLING.defaultInstance
                        "hunting" -> Items.LEAD.defaultInstance
                        "dungeoneering" -> SkullTextures.MORT.skull
                        else -> Items.BARRIER.defaultInstance
                    },
                    name.toTitleCase(),
                )
            } + MiscMuseumCategory
        }
    }

    override fun create(
        gameProfile: GameProfile,
        profile: SkyBlockProfile?,
    ): BaseWindowedPvScreen = CategorizedMuseumScreen(category, gameProfile, profile)

    override val isSelected: Boolean
        get() = (McScreen.self as? CategorizedMuseumScreen)?.category == category
}
