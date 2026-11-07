package me.owdding.skyblockpv.feature

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.utils.PlayerLevelCache
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.regex.component.match
import tech.thatgravyboat.skyblockapi.utils.regex.component.toComponentRegex
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object SkyBlockLevelInOtherChat {

    private val regex = "(?<first>.*(?:Party|Guild|Officer|Co-op) > )(?<second>(?:\\[.*] )?(?<name>\\w{1,16})(?: \\[.*])?: .+)".toRegex().toComponentRegex()

    @Subscription(priority = Subscription.LOW)
    fun onChat(event: ChatReceivedEvent.Post) {
        if (!Config.showLevelInOtherChat) return

        regex.match(event.component, "first", "second", "name") { (first, second, name) ->
            val level = PlayerLevelCache.getComponent(name.stripped) ?: return@match
            event.component = Text.of {
                append(first)
                append(level)
                append(second)
            }
        }
    }

}
