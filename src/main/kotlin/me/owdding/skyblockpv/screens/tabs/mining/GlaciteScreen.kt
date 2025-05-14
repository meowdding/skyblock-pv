package me.owdding.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.*
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.FossilTypes
import me.owdding.skyblockpv.data.api.skills.GlaciteData
import me.owdding.skyblockpv.data.repo.EssenceData.addMiningPerk
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.fitsIn
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold

class GlaciteScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    override fun getLayout(bg: DisplayWidget): Layout {
        val glacite = profile.glacite ?: return LayoutFactory.empty()
        val columnWidth = uiWidth / 2 - 10
        val normalLayout = LayoutFactory.horizontal(5) {
            spacer(height = uiHeight)
            vertical(5) {
                spacer()
                widget(getInfoWidget(columnWidth, glacite))
                widget(getFossilWidget(columnWidth, glacite))
            }
            vertical(5) {
                spacer()
                widget(getCorpseWidget(columnWidth, glacite))
            }
        }

        if (normalLayout.fitsIn(bg)) {
            return normalLayout
        }

        return LayoutFactory.vertical(5, alignment = 0.5f) {
            widget(getInfoWidget(width / 2, glacite))
            widget(getFossilWidget(width / 2, glacite))
            widget(getCorpseWidget(width / 2, glacite))
        }.asScrollable(bg.width - 10, bg.height)
    }

    private fun getInfoWidget(width: Int, glacite: GlaciteData) = PvWidgets.label(
        title = "Info",
        width = width,
        element = LayoutFactory.vertical(3) {
            fun grayText(text: String) = display(Displays.text(text, color = { 0x555555u }, shadow = false))
            val fossilDust = glacite.fossilDust

            grayText("Mineshaft Entered: ${glacite.mineshaftsEntered.toFormattedString()}")
            Displays.text("Fossil Dust: ${fossilDust.toFormattedString()}", color = { 0x555555u }, shadow = false)
                .withTooltip(getFossilDustConversions(fossilDust))
                .let { display(it) }

            addMiningPerk(profile, "frozen_skin")
        },
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    private fun getFossilWidget(width: Int, glacite: GlaciteData) = PvWidgets.label(
        title = "Fossils",
        width = width,
        element = FossilTypes.fossils.map {
            val unlocked = glacite.fossilsDonated.contains(it.id.split("_").first())
            val inPetMenu = profile.pets.any { pet -> pet.type == it.pet }

            val item = if (unlocked) RepoItemsAPI.getItem(it.id)
            else Items.GRAY_DYE.defaultInstance

            val hover = Text.multiline(
                Text.of(it.name).apply {
                    bold = true
                },
                Text.join(
                    Text.of("Donated: ").withColor(TextColor.GRAY),
                    Text.of(if (unlocked) "Yes" else "No").withColor(if (unlocked) TextColor.GREEN else TextColor.RED),
                ),
                Text.join(
                    Text.of("In Pet Menu: ").withColor(TextColor.GRAY),
                    Text.of(if (inPetMenu) "Yes" else "No").withColor(if (inPetMenu) TextColor.GREEN else TextColor.RED),
                    if (!inPetMenu) Text.of("§a${it.pet.toTitleCase()}").wrap(" §7(", "§7)") else null,
                ),
            )

            Displays.padding(2, Displays.item(item).withTooltip(hover))
        }.chunked(4).asTable(2).let {
            ExtraDisplays.inventoryBackground(
                4, 2,
                Displays.padding(2, it),
            )
        }.asWidget(),
    )


    private fun getCorpseWidget(width: Int, glacite: GlaciteData) = PvWidgets.label(
        "Corpses Looted",
        element = LayoutFactory.vertical(3) {
            fun addCorpse(name: String, color: Int) {
                string(
                    Text.join(
                        Text.of("${name.toTitleCase()} Corpses: ").withColor(TextColor.DARK_GRAY),
                        Text.of((glacite.corpsesLooted[name] ?: 0).toFormattedString()).withColor(color),
                    ),
                )
            }

            addCorpse("lapis", TextColor.BLUE)
            addCorpse("tungsten", TextColor.GRAY)
            addCorpse("umber", TextColor.GOLD)
            addCorpse("vanguard", TextColor.AQUA)
        },
        width = width - 5,
    )

    private val fossilDustConversions = mapOf(
        "Suspicious Scrap" to 500,
    )

    private fun getFossilDustConversions(fossilDust: Int) = Text.multiline(
        Text.of("Fossil Dust Conversions:").apply {
            bold = true
        },
        fossilDustConversions.map { (name, amount) ->
            val converted = fossilDust / amount
            val color = if (converted > 0) TextColor.GREEN else TextColor.RED
            Text.join(
                Text.of("$name: ").withColor(TextColor.GRAY),
                Text.of(converted.toFormattedString()).withColor(color),
                Text.of(" ($amount per)").withColor(TextColor.GRAY),
            )
        },
    )
}
