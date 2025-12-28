package me.owdding.skyblockpv.feature

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.utils.Utils.asTranslated
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.command
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover

@Module
object ClickableChatMessages {

    private val otherChatRegex = "(?:Party|Guild|Officer|Co-op) > (?:\\[.*] )?(?<username>\\w{1,16})(?: \\[.*])?: .+".toRegex()

    @Subscription
    @OnlyOnSkyBlock
    fun onAllChat(event: ChatReceivedEvent.Post) {
        if (!Config.profileChatClick) return

        var output = Component.empty()
        var hasReplaced = false

        for (component in event.component.siblings) {
            val command = (component.style.clickEvent as? ClickEvent.RunCommand)?.command()
            val hoverText = (component.style.hoverEvent as? HoverEvent.ShowText)?.value?.stripped
            if ((command?.startsWith("/socialoptions ") != true && command?.startsWith("/viewprofile ") != true) || hoverText == null) {
                output.append(component)
                continue
            }

            val username = getUsername(command, hoverText)
            if (username == null) {
                output = output.append(component)
                continue
            }
            val nameComponent = component.copy()
            nameComponent.command = "/sbpv pv $username"
            nameComponent.hover = "messages.click_to_open_pv".asTranslated(username)

            output = output.append(nameComponent)
            hasReplaced = true
        }

        if (!hasReplaced) return
        event.component = output
    }

    private val nameRegex = Regex("^Click here to view ((?i)[a-z0-9_]{1,16}?)'s profile$")

    fun getUsername(command: String, hoverText: String): String? {
        return if (command.startsWith("/socialoptions ")) command.substringAfter(" ")
        else hoverText.lines().firstOrNull { it.matches(nameRegex) }?.replace(nameRegex, "$1")
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
