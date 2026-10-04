package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover

@Module
@LoadData
object EmblemsCodecs : DefaultedData {

    val emblems: MutableList<Emblem> = mutableListOf()

    fun getEmblem(id: String) = emblems.find { it.id.equals(id, true) }
    fun isEmblemKnown(id: String) = emblems.map { it.id }.contains(id)

    override suspend fun load() {
        emblems.addAll(
            Utils.loadRemoteRepoData(
                "pv/emblems",
                SkyBlockPvCodecs.getCodec<Emblem>().listOf(),
            ),
        )
    }

    @GenerateCodec
    data class Emblem(
        val id: String,
        val family: String,
        val name: String,
        val icon: Component,
    )

    @Subscription
    fun onCommand(event: RegisterCommandsEvent) {
        event.registerWithCallback("sbpv dev emblems") {
            emblems.groupBy { it.family }.map {
                Text.of("${it.key}: ") {
                    color = TextColor.GREEN
                    it.value.forEach { emblem ->
                        append(emblem.icon) {
                            hover = Text.of("${emblem.name} - ${emblem.id}")
                        }
                    }
                }
            }.let { Text.multiline(it).sendWithPrefix() }
        }
    }
}
