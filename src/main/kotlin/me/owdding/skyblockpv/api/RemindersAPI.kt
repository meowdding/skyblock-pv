package me.owdding.skyblockpv.api

import com.google.gson.JsonArray
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Module
object RemindersAPI {

    private val CODEC = Reminder.CODEC.listOf()

    private val file = SkyBlockPv.configDir.resolve("reminders.json")
    private val reminders = mutableListOf<Reminder>()

    init {
        if (!file.exists()) {
            file.parent.createDirectories()
        } else {
            file.readText().readJson<JsonArray>().toData(CODEC)?.let {
                reminders.addAll(it)
                reminders.sortBy(Reminder::timestamp)
            }
        }
    }

    private fun save() {
        reminders.toJson(CODEC)?.let {
            file.writeText(it.toString())
        }
    }

    @Subscription
    fun onTick(event: TickEvent) {
        if (reminders.isEmpty()) return

        val now = System.currentTimeMillis()
        val iterator = reminders.iterator()
        while (iterator.hasNext()) {
            val reminder = iterator.next()
            if (reminder.timestamp <= now) {
                iterator.remove()
                McClient.chat.addMessage(reminder.message)
            } else {
                break
            }
        }
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

data class Reminder(
    val id: String,
    val message: Component,
    val timestamp: Long,
) {

    companion object {

        val CODEC = RecordCodecBuilder.create { instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Reminder::id),
            ComponentSerialization.CODEC.fieldOf("message").forGetter(Reminder::message),
            Codec.LONG.fieldOf("timestamp").forGetter(Reminder::timestamp),
        ).apply(instance, ::Reminder) }
    }
}
