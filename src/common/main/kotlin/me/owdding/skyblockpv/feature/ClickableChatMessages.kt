package me.owdding.skyblockpv.feature

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.utils.Utils.asTranslated
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.command
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover

@Module
object ClickableChatMessages {

    private val otherChatRegex = "(?:Party|Guild|Officer) > (?:\\[.*] )?(?<username>\\w{1,16})(?: \\[.*])?: .+".toRegex()

    @Subscription
    @OnlyOnSkyBlock
    fun onAllChat(event: ChatReceivedEvent.Post) {
        if (!Config.profileChatClick) return

        var output = Component.empty()
        var hasReplaced = false

        for (component in event.component.siblings) {
            val command = (component.style.clickEvent as? ClickEvent.RunCommand)?.command()
            if (command?.startsWith("/socialoptions ") != true) {
                output.append(component)
                continue
            }

            val username = command.removePrefix("/socialoptions ")
            val nameComponent = component.copy()
            nameComponent.command = "/sbpv pv $username"
            nameComponent.hover = "messages.click_to_open_pv".asTranslated(username)

            output = output.append(nameComponent)
            hasReplaced = true
        }

        if (!hasReplaced) return
        event.component = output
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onOtherChat(event: ChatReceivedEvent.Post) {
        if (!Config.profileChatClickOther) return

        otherChatRegex.match(event.text, "username") { (username) ->
            event.component = event.component.copy().apply {
                command = "/sbpv pv $username"
                hover = "messages.click_to_open_pv".asTranslated(username)
            }
        }
    }
}
