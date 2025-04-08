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
import tech.thatgravyboat.skyblockpv.data.repo.*
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.Utils.fixBase64Padding
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
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

    private fun BestiaryCategoryEntry.getCategory() = mobs
        .map { it.getItem() }
        .toMutableList()
        .rightPad(5, Displays.empty(16, 16))
        .map { Displays.padding(2, it) }
        .chunked(5)
        .let {
            Displays.inventoryBackground(
                5, it.size,
                Displays.padding(2, it.asTable()),
            )
        }

    private fun ComplexBestiaryCategoryEntry.getCategory() = subcategories
        .flatMap { it.value.mobs.map { it.getItem() } }
        .toMutableList()
        .rightPad(8, Displays.empty(16, 16))
        .map { Displays.padding(2, it) }
        .chunked(8)
        .let {
            Displays.inventoryBackground(
                8, it.size,
                Displays.padding(2, it.asTable()),
            )
        }

    private fun BestiaryMobEntry.getItem() = icon.getItem().apply {
        val lore = TooltipBuilder().apply {
            add(cap.toString())
            add(bracket.toString())
            add(mobs.joinToString(", "))
        }.build().split("\n")
        set(DataComponents.CUSTOM_NAME, Text.join(name) { italic = false; color = TextColor.WHITE })
        set(DataComponents.LORE, ItemLore(lore, lore))
    }.let { Displays.item(it, showTooltip = true) }

    private fun BestiaryIcon.getItem(name: String = ""): ItemStack = Either.unwrap(
        this.mapBoth(
            { Utils.getMinecraftItem(it) },
            { createSkull(it.second.split("\", \"").first().fixBase64Padding()) },
        ),
    ).apply {
        set(DataComponents.CUSTOM_NAME, Text.join(name) { italic = false; color = TextColor.WHITE })
    }
}

