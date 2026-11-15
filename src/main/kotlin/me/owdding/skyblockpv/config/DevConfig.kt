package me.owdding.skyblockpv.config

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import kotlinx.coroutines.runBlocking
import me.owdding.lib.utils.config.AutoTranslated
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.PvAPI
import net.minecraft.client.gui.components.toasts.SystemToast
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text

object DevConfig : CategoryKt("Dev"), AutoTranslated {

    override val translationBase: String = "skyblockpv.dev"

    var devMode by autoBoolean(false)

    var hoppityParser by autoBoolean(false)

    var sacksParser by autoBoolean(false)

    var offlineMode by autoBoolean(false) {
        condition = { devMode }
    }

    init {
        if (SkyBlockPv.isSuperUser) {
            autoButton("bypass_cache") {
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
