package me.owdding.skyblockpv.config

import com.google.gson.JsonObject
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import me.owdding.lib.utils.config.AutoTranslated
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider.SuggestionTypes
import me.owdding.skyblockpv.feature.PartyFinderJoin
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.resources.Identifier
import java.util.function.UnaryOperator

object Config : ConfigKt("skyblockpv/config"), AutoTranslated {

    override val translationBase: String
        get() = "skyblockpv.config"
    override val name get() = Literal("SkyBlock Profile Viewer")
    override val description get() = Literal("Making Modern Pv'able (v${SkyBlockPv.VERSION})")
    override val links: Array<ResourcefulConfigLink>
        get() = arrayOf(
            ResourcefulConfigLink.create(
                "https://discord.gg/FsRc2GUwZR",
                "discord",
                Literal("Discord"),
            ),
            ResourcefulConfigLink.create(
                "https://modrinth.com/project/skyblock-profile-viewer",
                "modrinth",
                Literal("Modrinth"),
            ),
            ResourcefulConfigLink.create(
                "https://github.com/meowdding/skyblock-pv",
                "code",
                Literal("GitHub"),
            ),
        )

    init {
        category(DevConfig)
    }

    var profileSpying by boolean(true) { this.translation = "skyblockpv.config.spying" }
    var profileChatClick by boolean(true) { this.translation = "skyblockpv.config.chat" }
    var profileChatClickOther by boolean(false) { this.translation = "skyblockpv.config.chat_other" }
    var currency by autoEnum(ConfigCurrency.USD)
    var alignCategoryButtonsLeft by autoBoolean(true)
    var showPronouns by autoBoolean(true)
    var partyFinderMessage by autoEnum(PartyFinderJoin.State.OPEN_PV)
    var disableOutsideHypixel by autoBoolean(false)
    var skillOverflow by autoBoolean(false)
    var displayScaling by autoBoolean(false)
    var theme by transform(
        string("skyblock-pv:default") {
            renderer = THEME_RENDERER
        },
        { it.toString() },
        { Identifier.tryParse(it) ?: id("normal") },
    )
    var seenAutoThemes by strings { condition = { false } }
    var socials by autoBoolean(true)
    var rememberLastTab by autoBoolean(true)
    var autocompleteSources by autoEnums(value = SuggestionTypes.entries.toTypedArray())
    var replaceStatisticsButton by autoBoolean(true)

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
        2 to UnaryOperator { json ->
            if (json.get("currency").asString == "ZWL") {
                json.addProperty("currency", "ZWG")
            }
            json
        },
    )
    override val version get() = patches.size
}
