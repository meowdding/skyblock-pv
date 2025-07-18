package me.owdding.skyblockpv.feature

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.utils.Utils.asTranslated
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.command
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover

@Module
object ClickableChatMessages {

    @Subscription
    @OnlyOnSkyBlock
    fun onChat(event: ChatReceivedEvent.Post) {
        if (!Config.profileChatClick) return

        var output = Component.empty()
        var hasReplaced = false

        for (component in event.component.siblings) {
            val command = (component.style.clickEvent as? ClickEvent.RunCommand)?.command
            if (command?.startsWith("/socialoptions ") != true) {
                output.append(component)
                continue
            }

            val username = command.removePrefix("/socialoptions ")
            val nameComponent = component.copy()
            nameComponent.command = "/sbpv pv $username"
            nameComponent.hover = "messages.social_options_hover".asTranslated(username)

            output = output.append(nameComponent)
            hasReplaced = true
        }

        if (!hasReplaced) return
        event.component = output
    }
}
