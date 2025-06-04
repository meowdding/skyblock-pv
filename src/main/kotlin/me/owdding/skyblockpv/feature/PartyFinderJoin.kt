package me.owdding.skyblockpv.feature

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils.asTranslated
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match

@Module
object PartyFinderJoin {

    private val joinMessageRegex = "Party Finder > (?<username>.*) joined the dungeon group!.*".toRegex()

    @Subscription
    fun onChat(event: ChatReceivedEvent.Pre) {
        if (!Config.partyFinderMessage) return

        joinMessageRegex.match(event.text, "username") { (username) ->
            if (username.equals(McPlayer.name, ignoreCase = true)) {
                return@match
            }

            McClient.tell {
                "messages.party_finder_join".asTranslated(username).sendWithPrefix()
            }
        }
    }

}
