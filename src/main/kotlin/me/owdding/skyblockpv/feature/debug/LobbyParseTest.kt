package me.owdding.skyblockpv.feature.debug

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.ProfileAPI
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.time.since
import java.util.concurrent.CompletableFuture

@Module
object LobbyParseTest {

    @Subscription
    fun onCommand(event: RegisterCommandsEvent) {
        if (!SkyBlockPv.isSuperUser) return

        event.registerWithCallback("sbpv testlobby") {
            var successfulParses = 0
            var failedParses = 0
            val startTime = Clock.System.now()
            CompletableFuture.runAsync {
                runBlocking {
                    McClient.players.forEach {
                        val profile = it.profile
                        val failed = ProfileAPI.getProfiles(profile).isEmpty()

                        if (failed) {
                            Text.of {
                                color = TextColor.RED
                                append("Failed to get profiles for ")
                                append(profile.name)
                            }.sendWithPrefix()
                            failedParses++
                        } else {
                            Text.of {
                                color = TextColor.GREEN
                                append("Successfully got profiles for ")
                                append(profile.name)
                            }.sendWithPrefix()
                            successfulParses++
                        }

                        Thread.sleep(100)
                    }

                    val diff = startTime.since().toReadableTime(allowMs = true)
                    Text.of {
                        color = TextColor.PINK
                        append("Finished parsing after ")
                        append(diff)
                        append(" with ")
                        append(successfulParses.toString()) {
                            color = TextColor.GREEN
                        }
                        append(" successful parses ") {
                            color = TextColor.GREEN
                        }
                        append("and ")
                        append(failedParses.toString()) {
                            color = TextColor.RED
                        }
                        append(" failed parses") {
                            color = TextColor.RED
                        }
                    }.sendWithPrefix()
                }
            }
        }
    }

}
