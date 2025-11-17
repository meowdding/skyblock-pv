package me.owdding.skyblockpv.feature.debug

import kotlinx.coroutines.runBlocking
import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.ProfileAPI
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.theme.PvColors
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
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
            val startTime = currentInstant()
            CompletableFuture.runAsync {
                runBlocking {
                    McClient.players.forEach {
                        val profile = it.profile
                        val failed = ProfileAPI.getProfiles(profile).isEmpty()

                        if (failed) {
                            Text.of {
                                color = PvColors.RED
                                append("Failed to get profiles for ")
                                append(profile.name)
                            }.sendWithPrefix()
                            failedParses++
                        } else {
                            Text.of {
                                color = PvColors.GREEN
                                append("Successfully got profiles for ")
                                append(profile.name)
                            }.sendWithPrefix()
                            successfulParses++
                        }

                        Thread.sleep(100)
                    }

                    val diff = startTime.since().toReadableTime(allowMs = true)
                    Text.of {
                        color = PvColors.PINK
                        append("Finished parsing after ")
                        append(diff)
                        append(" with ")
                        append(successfulParses.toString()) {
                            color = PvColors.GREEN
                        }
                        append(" successful parses ") {
                            color = PvColors.GREEN
                        }
                        append("and ")
                        append(failedParses.toString()) {
                            color = PvColors.RED
                        }
                        append(" failed parses") {
                            color = PvColors.RED
                        }
                    }.sendWithPrefix()
                }
            }
        }
    }

}
