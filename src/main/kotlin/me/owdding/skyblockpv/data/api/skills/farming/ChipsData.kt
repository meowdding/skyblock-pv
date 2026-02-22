package me.owdding.skyblockpv.data.api.skills.farming

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.ParseHelper

class ChipsData(override val json: JsonObject) : ParseHelper {
    val cropshot by int()
    val evergreen by int()
    val hypercharge by int()
    val mechamind by int()
    val overdrive by int()
    val quickdraw by int()
    val rarefinder by int()
    val sowledge by int()
    val synthesis by int()
    val verminVaporizer by int("vermin_vaporizer")

    companion object {
        val EMPTY = ChipsData(JsonObject())
    }
}
