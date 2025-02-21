package tech.thatgravyboat.skyblockpv.screens.tabs

import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyblockProfile
import tech.thatgravyboat.skyblockpv.data.getIconFromSkillName
import tech.thatgravyboat.skyblockpv.data.getSkillLevel
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.util.*

class MainScreen(uuid: UUID) : BasePvScreen("MAIN", uuid) {
    override suspend fun create(bg: DisplayWidget) {
        val profile = ProfileAPI.getProfiles(uuid).find { it.selected } ?: return
        val middleColumnWidth = (uiWidth * 0.2).toInt()
        val sideColumnWidth = (uiWidth - middleColumnWidth) / 2

        val cols = LinearLayout.horizontal()

        val col1 = Displays.background(0x40FF0000u, Displays.fixed(sideColumnWidth, uiHeight, Displays.text("§8Col 1"))).asWidget()

        cols.addChild(col1)
        cols.addChild(createMiddleColumn(profile, middleColumnWidth))
        cols.addChild(createRightColumn(profile, sideColumnWidth))

        cols.arrangeElements()
        cols.setPosition(bg.x, bg.y)
        cols.visitWidgets(this::addRenderableWidget)
    }

    fun createMiddleColumn(profile: SkyblockProfile, width: Int): DisplayWidget {
        val column = buildList<Display> {
            // TODO: load gameprofile from uuid and create a custom player from the gameprofile
            Displays.entity(McPlayer.self!!, width, width, 40).withBackground(0xD0000000u).centerIn(-1, uiHeight).also { add(it) }
        }.toColumn()

        return Displays.background(0x400000FFu, Displays.fixed(width, uiHeight, column)).asWidget()
    }

    fun createRightColumn(profile: SkyblockProfile, width: Int): DisplayWidget {
        val column = buildList {
            add(Displays.text("Skills"))
            val skillDisplayElementWidth = 30
            val skillElementsPerRow = width / skillDisplayElementWidth

            profile.skill.asSequence().chunked(skillElementsPerRow).map { chunk ->
                chunk.map { (skill, data) ->
                    val level = getSkillLevel(skill, data)
                    listOf(
                        Displays.sprite(getIconFromSkillName(skill), 12, 12),
                        Displays.text("$level"),
                    ).toRow(1)
                }.toRow(5)
            }.toList().toColumn(5).also { add(it) }

            add(Displays.text("Slayer"))
            add(Displays.text("Collection"))
        }.toColumn()

        return Displays.background(0x4000FF00u, Displays.fixed(width, uiHeight, column)).asWidget()
    }
}
