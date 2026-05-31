package me.owdding.skyblockpv.feature.debug

import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.api.PlayerAPI
import me.owdding.skyblockpv.api.PvAPI
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils.fetchGameProfile
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text

@Module
object TestRank {

    @Subscription
    fun onCommand(event: RegisterCommandsEvent) {
        event.register("sbpv dev testRank") {
            then("player", StringArgumentType.string(), SkyBlockPlayerSuggestionProvider) {
                callback {
                    if (!PvAPI.isAuthenticated()) {
                        (+"messages.api.not_authenticated").sendWithPrefix()
                        return@callback
                    }
                    val name = this.getArgument("player", String::class.java)
                    fetchGameProfile(name) {
                        PlayerAPI.getPlayer(it ?: return@fetchGameProfile) { data ->
                            Text.of("Rank of $name: ") {
                                append(data?.prefix)
                            }.sendWithPrefix()
                        }
                    }
                }
            }
        }
    }
}
