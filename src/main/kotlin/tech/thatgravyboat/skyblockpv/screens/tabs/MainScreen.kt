package tech.thatgravyboat.skyblockpv.screens.tabs

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import org.apache.commons.lang3.function.Consumers
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyblockProfile
import tech.thatgravyboat.skyblockpv.data.getIconFromSkillName
import tech.thatgravyboat.skyblockpv.data.getSkillLevel
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.FakePlayer
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.util.*

class MainScreen(uuid: UUID) : BasePvScreen("MAIN", uuid) {
    override suspend fun create(bg: DisplayWidget) {
        val profiles = ProfileAPI.getProfiles(uuid)
        val profile = profiles.find { it.selected } ?: return
        val middleColumnWidth = (uiWidth * 0.2).toInt()
        val sideColumnWidth = (uiWidth - middleColumnWidth) / 2

        val cols = LinearLayout.horizontal()

        val col1 = Displays.background(0x40FF0000u, Displays.fixed(sideColumnWidth, uiHeight, Displays.text("§8Col 1"))).asWidget()

        cols.addChild(col1)
        cols.addChild(createMiddleColumn(profiles, middleColumnWidth))
        cols.addChild(createRightColumn(profile, sideColumnWidth))

        cols.arrangeElements()
        cols.setPosition(bg.x, bg.y)
        cols.visitWidgets(this::addRenderableWidget)
    }

    fun createMiddleColumn(profiles: List<SkyblockProfile>, width: Int): LinearLayout {
        val fakeProfile = McClient.self.minecraftSessionService.fetchProfile(uuid, false)?.profile ?: McPlayer.self!!.gameProfile
        val playerWidget = Displays.placeholder(width, width).asWidget().withRenderer { gr, ctx, _ ->
            Displays.entity(
                FakePlayer(fakeProfile),
                width, width,
                width / 2,
                ctx.mouseX.toFloat() - ctx.x, ctx.mouseY.toFloat() - ctx.y,
            ).withBackground(0xD0000000u).render(gr, ctx.x, ctx.y)
        }

        val layout = LinearLayout.vertical()
        layout.addChild(SpacerElement.height((uiHeight - playerWidget.height) / 2))
        layout.addChild(playerWidget)

        val state = DropdownState<SkyblockProfile>.of<SkyblockProfile>(profiles.find { it.selected } ?: profiles.first())
        val dropdown = Widgets.dropdown(
            state,
            profiles,
            { profile ->
                var profileName = profile.id.name
                when (profile.profileType) {
                    ProfileType.NORMAL -> {}
                    ProfileType.BINGO -> profileName += " Ⓑ"
                    ProfileType.IRONMAN -> profileName += " ♻"
                    ProfileType.STRANDED -> profileName += " ☀"
                    ProfileType.UNKNOWN -> {}
                }
                return@dropdown Text.of(profileName)
            },
            { button -> button.withSize(width, 20) },
            Consumers.nop(), // TODO: make actually function
        )
        layout.addChild(dropdown)

        return layout
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
                }.toRow(5).centerIn(width, -1)
            }.toList().toColumn(5).also { add(it) }

            add(Displays.text("Slayer"))
            add(Displays.text("Collection"))
        }.toColumn()

        return Displays.background(0x4000FF00u, Displays.fixed(width, uiHeight, column)).asWidget()
    }
}
