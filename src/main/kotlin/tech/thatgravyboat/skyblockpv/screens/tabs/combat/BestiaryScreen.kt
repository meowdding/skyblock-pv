package tech.thatgravyboat.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Either
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.repo.*
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.Utils.fixBase64Padding
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.createSkull
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.withTooltip

class BestiaryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    private var carousel: CarouselWidget? = null
    private val MOBS_PER_ROW_SIMPLE = 5
    private val MOBS_PER_ROW_COMPLEX = 8

    override fun getLayout(bg: DisplayWidget) = LayoutBuild.vertical {
        val categories = getCategories()
        val inventories = categories.values.toList()
        val icons = categories.keys.toList()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val buttonContainer = carousel!!.getIcons { icons.map { Displays.item(it, showTooltip = true) } }

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))
    }

    private fun getCategories(): Map<ItemStack, Display> = BestiaryCodecs.data?.categories?.map { (_, v) ->
        Either.unwrap(
            v.mapBoth(
                { it.icon.getItem(it.name) to it.getCategory() }, // Simple
                { it.icon.getItem(it.name) to it.getCategory() }, // Complex
            ),
        )
    }?.toMap() ?: emptyMap()

    private fun BestiaryCategoryEntry.getCategory() = mobs.map { it.getItem() }.format(MOBS_PER_ROW_SIMPLE)

    private fun ComplexBestiaryCategoryEntry.getCategory() = subcategories.flatMap { it.value.mobs.map { it.getItem() } }.format(MOBS_PER_ROW_COMPLEX)

    private fun List<Display>.format(mobsPerRow: Int) = toMutableList()
        .rightPad(mobsPerRow * 2, Displays.empty(16, 16))
        .map { Displays.padding(2, it) }
        .chunked(mobsPerRow)
        .let {
            Displays.inventoryBackground(
                mobsPerRow, it.size,
                Displays.padding(2, it.asTable()),
            )
        }

    private fun BestiaryMobEntry.getItem(): Display {
        val kills = profile?.bestiaryData?.filter { mobs.contains(it.mobId) }?.sumOf { it.kills } ?: 0
        val item = icon.getItem().withTooltip {
            add(name)
            add(cap.toString())
            add(bracket.toString())
            add("Total Kills: $kills") {
                color = TextColor.GRAY
            }
        }
        return Displays.item(item, customStackText = kills.shorten(0), showTooltip = true)
    }

    private fun BestiaryIcon.getItem(name: String = ""): ItemStack = Either.unwrap(
        this.mapBoth(
            { Utils.getMinecraftItem(it) },
            { createSkull(it.second.split("\", \"").first().fixBase64Padding()) },
        ),
    ).apply {
        set(DataComponents.CUSTOM_NAME, Text.join(name) { italic = false; color = TextColor.WHITE })
    }
}

