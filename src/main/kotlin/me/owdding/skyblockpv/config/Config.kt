package me.owdding.skyblockpv.config

import com.google.gson.JsonObject
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.feature.PartyFinderJoin
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.resources.Identifier
import java.util.function.UnaryOperator

object Config : ConfigKt("skyblockpv/config") {

    override val name get() = TranslatableValue("SkyBlock Profile Viewer")
    override val description get() = TranslatableValue("Making Modern Pv'able (v${SkyBlockPv.version})")
    override val links: Array<ResourcefulConfigLink>
        get() = arrayOf(
            ResourcefulConfigLink.create(
                "https://discord.gg/FsRc2GUwZR",
                "discord",
                TranslatableValue("Discord"),
            ),
            ResourcefulConfigLink.create(
                "https://modrinth.com/project/skyblock-profile-viewer",
                "modrinth",
                TranslatableValue("Modrinth"),
            ),
            ResourcefulConfigLink.create(
                "https://github.com/meowdding/skyblock-pv",
                "code",
                TranslatableValue("GitHub"),
            ),
        )

    init {
        category(DevConfig)
    }

    var profileSpying by boolean(true) { this.translation = "skyblockpv.config.spying" }
    var profileChatClick by boolean(true) { this.translation = "skyblockpv.config.chat" }
    var showLevelInOtherChat by boolean(false) { this.translation = "skyblockpv.config.show_level_in_other_chat" }
    var profileChatClickOther by boolean(false) { this.translation = "skyblockpv.config.chat_other" }
    var currency by enum(ConfigCurrency.USD) { this.translation = "skyblockpv.config.currency" }
    var alignCategoryButtonsLeft by boolean(true) { this.translation = "skyblockpv.config.align_category_buttons_left" }
    var showPronouns by boolean(true) { this.translation = "skyblockpv.config.show_pronouns" }
    var partyFinderMessage by enum(PartyFinderJoin.State.OPEN_PV) { this.translation = "skyblockpv.config.party_finder_message" }
    var disableOutsideHypixel by boolean(false) { this.translation = "skyblockpv.config.disable_outside_hypixel" }
    var skillOverflow by boolean(false) { this.translation = "skyblockpv.config.skill_overflow" }
    var displayScaling by boolean(false) { this.translation = "skyblockpv.config.display_scaling" }
    var theme by transform(
        string("skyblock-pv:default") {
            renderer = THEME_RENDERER
        },
        { it.toString() },
        { Identifier.tryParse(it) ?: id("normal") },
    )
    var socials by boolean(true) { this.translation = "skyblockpv.config.socials" }
    var rememberLastTab by boolean(true) { this.translation = "skyblockpv.config.remember_last_tab" }

    val isDisabled: Boolean get() = disableOutsideHypixel && !Utils.onHypixel

    override val patches: Map<Int, UnaryOperator<JsonObject>> = mapOf(
        0 to UnaryOperator { json ->
            val state = if (json.get("partyFinderMessage")?.asBoolean ?: false) PartyFinderJoin.State.OPEN_PV else PartyFinderJoin.State.OFF
            json.addProperty("partyFinderMessage", state.name)
            json
        },
        1 to UnaryOperator { json ->
            val state = when (json.get("partyFinderMessage")?.asString?.uppercase() ?: "") {
                "OPEN_PV" -> PartyFinderJoin.State.OPEN_PV
                "BREAKDOWN" -> PartyFinderJoin.State.BREAKDOWN
                else -> PartyFinderJoin.State.OFF
            }
            json.addProperty("partyFinderMessage", state.name)
            json
        },
    )
    override val version get() = patches.size
}
