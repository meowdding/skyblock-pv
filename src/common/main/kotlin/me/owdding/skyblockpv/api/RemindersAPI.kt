package me.owdding.skyblockpv.api

import com.google.gson.JsonArray
import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Module
object RemindersAPI {

    private val file = SkyBlockPv.configDir.resolve("reminders.json")
    private val reminders = mutableListOf<Reminder>()

    @IncludedCodec(named = "reminder§message")
    val component: Codec<Component> = ComponentSerialization.CODEC

    init {
        if (!file.exists()) {
            file.parent.createDirectories()
        } else {
            file.readText().readJson<JsonArray>().toData(SkyBlockPVCodecs.getCodec<Reminder>().listOf())?.let {
                reminders.addAll(it)
                reminders.sortBy(Reminder::timestamp)
            }
        }
    }

    private fun save() {
        reminders.toJson(SkyBlockPVCodecs.getCodec<Reminder>().listOf())?.let {
            file.writeText(it.toString())
        }
    }

    @Subscription
    fun onTick(event: TickEvent) {
        if (reminders.isEmpty()) return

        val now = System.currentTimeMillis()
        val iterator = reminders.iterator()
        var remindersChanged = false
        while (iterator.hasNext()) {
            val reminder = iterator.next()
            if (reminder.timestamp <= now) {
                iterator.remove()
                reminder.message.sendWithPrefix()
                remindersChanged = true
            } else {
                break
            }
        }

        if (remindersChanged) save()
    }

    fun getReminders(): List<Reminder> {
        return reminders
    }

    fun addReminder(id: String, message: Component, timestamp: Long) {
        reminders.add(Reminder(id, message, timestamp))
        reminders.sortBy(Reminder::timestamp)
        save()
    }

    fun removeReminder(id: String) {
        reminders.removeIf { it.id == id }
        reminders.sortBy(Reminder::timestamp)
        save()
    }


}

@GenerateCodec
data class Reminder(
    val id: String,
    @NamedCodec("reminder§message") val message: Component,
    val timestamp: Long,
)
