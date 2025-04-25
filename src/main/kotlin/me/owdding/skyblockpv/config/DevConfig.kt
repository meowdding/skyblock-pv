package me.owdding.skyblockpv.config

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import kotlinx.coroutines.runBlocking
import me.owdding.skyblockpv.api.PvAPI
import net.minecraft.client.gui.components.toasts.SystemToast
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text

object DevConfig : CategoryKt("Dev") {

    private val SUPER_USERS = setOf(
        "503450fc-72c2-4e87-8243-94e264977437",
        "e90ea9ec-080a-401b-8d10-6a53c407ac53",
        "b75d7e0a-03d0-4c2a-ae47-809b6b808246"
    )

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

    init {
        if (McClient.self.user.profileId.toString() in SUPER_USERS) {
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
