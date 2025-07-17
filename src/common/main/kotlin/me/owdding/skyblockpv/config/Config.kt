package me.owdding.skyblockpv.config

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import me.owdding.skyblockpv.McVersion
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.resources.ResourceLocation

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
    var currency by enum(ConfigCurrency.USD) { this.translation = "skyblockpv.config.currency" }
    var alignCategoryButtonsLeft by boolean(true) { this.translation = "skyblockpv.config.align_category_buttons_left" }
    var showPronouns by boolean(true) { this.translation = "skyblockpv.config.show_pronouns" }
    var partyFinderMessage by boolean(true) { this.translation = "skyblockpv.config.party_finder_message" }
    var disableOutsideHypixel by boolean(false) { this.translation = "skyblockpv.config.disable_outside_hypixel" }
    var displayScaling by boolean(false) scaling@{
        if (McVersion.MC_1_21_8.isActive()) {
            this.translation = "skyblockpv.config.display_scaling.1218"
            return@scaling
        }

        this.translation = "skyblockpv.config.display_scaling"
    }
    var theme by transform(
        string("skyblock-pv:default") {
            renderer = THEME_RENDERER
        },
        { it.toString() },
        { ResourceLocation.tryParse(it) ?: id("normal") },
    )

    val isDisabled: Boolean get() = disableOutsideHypixel && !Utils.onHypixel
}
