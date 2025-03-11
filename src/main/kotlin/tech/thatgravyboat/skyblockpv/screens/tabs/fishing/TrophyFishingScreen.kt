package tech.thatgravyboat.skyblockpv.screens.tabs.fishing

import com.mojang.authlib.GameProfile
import net.minecraft.ChatFormatting
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.TrophyFish
import tech.thatgravyboat.skyblockpv.data.TrophyFishTiers
import tech.thatgravyboat.skyblockpv.data.TrophyFishTypes
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.Utils.getMainContentWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.getTitleWidget
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.text.DecimalFormat
import javax.swing.text.NumberFormatter

class TrophyFishingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("Fishing", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        val profile = profile ?: return

        val trophyTable = TrophyFishTiers.entries.map { tier ->
            buildList {
                TrophyFishTypes.entries.map { type ->
                    this.add(getElement(TrophyFish(type, tier), profile))
                }
            }
        }.asTable(10).centerIn(uiWidth, -1).asWidget()

        LayoutBuild.vertical {
            spacer(height = 5)
            widget(getTitleWidget("Trophy Fish", uiWidth))
            widget(getMainContentWidget(trophyTable, uiWidth))
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)
    }

    private fun getElement(trophyFish: TrophyFish, profile: SkyBlockProfile): Display {

        val amountCaught = profile.trophyFish.obtainedTypes.getOrDefault(trophyFish.apiName, 0)

        val item = if (!profile.trophyFish.obtainedTypes.containsKey(trophyFish.apiName)) {
            Displays.item(Items.GRAY_DYE.defaultInstance)
        } else {
            Displays.item(trophyFish.item, customStackText = DecimalFormat.getCompactNumberInstance().format(amountCaught))
        }


        return item.withTooltip(
            trophyFish.displayName,
            Text.of("Caught: $amountCaught") {
                withStyle(ChatFormatting.GRAY)
            },
        )
    }
}
