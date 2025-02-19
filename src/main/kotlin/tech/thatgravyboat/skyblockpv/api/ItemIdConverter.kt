package tech.thatgravyboat.skyblockpv.api

import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson

object ItemIdConverter {
    var conversionList: Map<String, String> = emptyMap()
        private set

    fun convertOldIdOrOldId(id: String) = conversionList[id] ?: id

    init {
        runBlocking {
            try {
                conversionList = this.javaClass.getResourceAsStream("/repo/1_8_9_to_1_21_1.json")?.readJson<Map<String, String>>() ?: return@runBlocking
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}

