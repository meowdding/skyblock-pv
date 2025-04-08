package tech.thatgravyboat.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Either
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.split
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.repo.BestiaryCategoryEntry
import tech.thatgravyboat.skyblockpv.data.repo.BestiaryCodecs
import tech.thatgravyboat.skyblockpv.data.repo.BestiaryIcon
import tech.thatgravyboat.skyblockpv.data.repo.ComplexBestiaryCategoryEntry
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.Utils.fixBase64Padding
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.createSkull
import tech.thatgravyboat.skyblockpv.utils.displays.*

class BestiaryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    private var carousel: CarouselWidget? = null

    override fun getLayout(bg: DisplayWidget) = LayoutBuild.vertical {
        val categories = getCategories()
        val inventories = categories.values.toList()
        val icons = categories.keys.toList()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val buttonContainer = carousel!!.getIcons {
            icons.mapIndexed { index, it ->
                Displays.item(it, showTooltip = true)
            }
        }

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))
    }

    private fun getCategories(): Map<ItemStack, Display> = BestiaryCodecs.data?.categories?.map { (_, v) ->
        Either.unwrap(
            v.mapBoth(
                { it.icon.getItem(it.name) to it.getCategory() },
                { it.icon.getItem(it.name) to it.getCategory() },
            ),
        )
    }?.toMap() ?: emptyMap()

    private fun BestiaryCategoryEntry.getCategory() = mobs.map {
        it.icon.getItem().apply {
            val lore = TooltipBuilder().apply {
                add(it.cap.toString())
                add(it.bracket.toString())
                add(it.mobs.joinToString(", "))
            }.build().split("\n")
            set(DataComponents.CUSTOM_NAME, Text.join(it.name) { italic = false; color = TextColor.WHITE })
            set(DataComponents.LORE, ItemLore(lore, lore))
        }.let { Displays.item(it, showTooltip = true) }
    }.toRow()

    private fun ComplexBestiaryCategoryEntry.getCategory() = Displays.empty()

    private fun BestiaryIcon.getItem(name: String = ""): ItemStack = Either.unwrap(
        this.mapBoth(
            { Utils.getMinecraftItem(it) },
            { createSkull(it.second.split("\", \"").first().fixBase64Padding()) },
        ),
    ).apply {
        set(DataComponents.CUSTOM_NAME, Text.join(name) { italic = false; color = TextColor.WHITE })
    }
}

