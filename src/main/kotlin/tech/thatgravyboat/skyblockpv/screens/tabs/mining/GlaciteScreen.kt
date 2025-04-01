package tech.thatgravyboat.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.EssenceData.addMiningPerk
import tech.thatgravyboat.skyblockpv.data.skills.mining.FossilTypes
import tech.thatgravyboat.skyblockpv.data.skills.mining.GlaciteData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.toTitleCase
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import tech.thatgravyboat.skyblockpv.utils.displays.withTooltip

class GlaciteScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    override fun getLayout(): Layout {
        val profile = profile ?: return LayoutBuild.horizontal { }
        val glacite = profile.glacite ?: return LayoutBuild.horizontal { }
        val columnWidth = uiWidth / 2

        return LayoutBuild.horizontal(5) {
            spacer(height = uiHeight)
            widget(createLeftColumn(profile, columnWidth))
            widget(createRightColumn(glacite, columnWidth))
        }
    }

    private fun createLeftColumn(profile: SkyBlockProfile, width: Int) = LayoutBuild.vertical {
        val glacite = profile.glacite ?: return@vertical
        spacer(width, 5)

        val info = LayoutBuild.vertical(3) {
            fun grayText(text: String) = display(Displays.text(text, color = { 0x555555u }, shadow = false))
            val fossilDust = glacite.fossilDust

            grayText("Mineshaft Entered: ${glacite.mineshaftsEntered.toFormattedString()}")
            Displays.text("Fossil Dust: ${fossilDust.toFormattedString()}", color = { 0x555555u }, shadow = false)
                .withTooltip(getFossilDustConversions(fossilDust))
                .let { display(it) }

            addMiningPerk(profile, "frozen_skin")
        }

        widget(PvWidgets.getTitleWidget("Info", width - 5))
        widget(PvWidgets.getMainContentWidget(info, width - 5))

        spacer(width, 5)

        val fossils = FossilTypes.fossils.map {
            val unlocked = glacite.fossilsDonated.contains(it.id.split("_").first())
            val inPetMenu = profile.pets.any { pet -> pet.type == it.pet }

            val item = if (unlocked) ItemAPI.getItem(it.id)
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
            Displays.inventoryBackground(
                4, 2,
                Displays.padding(2, it),
            )
        }.asWidget()

        widget(PvWidgets.getTitleWidget("Fossils", width - 5))
        widget(PvWidgets.getMainContentWidget(fossils, width - 5))

    }

    private fun createRightColumn(glacite: GlaciteData, width: Int) = LayoutBuild.vertical(alignment = 0.5f) {
        spacer(width, 5)

        val corpses = LayoutBuild.vertical(3) {
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
        }

        widget(PvWidgets.getTitleWidget("Corpses Looted", width - 5))
        widget(PvWidgets.getMainContentWidget(corpses, width - 5))
    }

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
