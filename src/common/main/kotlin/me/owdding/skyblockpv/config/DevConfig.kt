package me.owdding.skyblockpv.config

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import kotlinx.coroutines.runBlocking
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.PvAPI
import net.minecraft.client.gui.components.toasts.SystemToast
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text

object DevConfig : CategoryKt("Dev") {

    var devMode by boolean(false) {
        name = Translated("skyblockpv.dev.dev_mode")
        description = Translated("skyblockpv.dev.dev_mode.desc")
    }

    var hoppityParser by boolean(false) {
        name = Translated("skyblockpv.dev.hoppity_parser")
        description = Translated("skyblockpv.dev.hoppity_parser.desc")
    }

    var sacksParser by boolean(false) {
        name = Translated("skyblockpv.dev.sacks_parser")
        description = Translated("skyblockpv.dev.sacks_parser.desc")
    }

    var offlineMode by boolean(false) {
        condition = { devMode }
        translation = "skyblockpv.dev.offline_mode"
    }

    init {
        if (SkyBlockPv.isSuperUser) {
            button {
                title = "skyblockpv.dev.bypass_cache"
                description = "skyblockpv.dev.bypass_cache.desc"
                text = "Bypass Cache"
                onClick {
                    runBlocking {
                        PvAPI.authenticate(true)
                        if (PvAPI.isAuthenticated()) {
                            SystemToast.add(
                                McClient.toasts,
                                SystemToast.SystemToastId.WORLD_BACKUP,
                                Text.of("Cache Bypassed"),
                                null,
                            )
                        } else {
                            SystemToast.add(
                                McClient.toasts,
                                SystemToast.SystemToastId.WORLD_BACKUP,
                                Text.of("Failed to bypass cache"),
                                null,
                            )
                        }
                    }
                }
            }
        }
    }
}
