package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.utils.asBoolean
import tech.thatgravyboat.skyblockpv.utils.asInt
import tech.thatgravyboat.skyblockpv.utils.asLong
import java.util.*

data class Pet(
    val uuid: UUID?,
    val uniqueId: UUID?,
    val type: String,
    val exp: Long,
    val active: Boolean,
    val tier: String,
    val heldItem: String?,
    val candyUsed: Int,
    val skin: String?,
) {
    val cumulativeLevels = petLevels.drop(petRarityOffset[tier]!!).take(99).runningFold(0L) { a, b -> a + b }
    val rarity = SkyBlockRarity.entries.find { it.name == tier } ?: SkyBlockRarity.COMMON

    val itemStack by lazy { ItemAPI.getPet(type, rarity, level, skin) }

    val level = cumulativeLevels.let { lvls ->
        lvls.findLast { it <= exp }?.let { lvls.indexOf(it) }?.plus(1) ?: 1
    }

    val progressToNextLevel: Float = run {
        if (level == 100) return@run 1f

        val currXp = cumulativeLevels[(level - 1).coerceAtLeast(0)]
        ((exp.toFloat() - currXp) / (cumulativeLevels[level] - currXp))
    }
    val progressToMax: Float = (exp.toFloat() / cumulativeLevels.last()).coerceAtMost(1f)

    companion object {
        fun fromJson(obj: JsonObject) = Pet(
            uuid = obj["uuid"]?.takeIf { it !is JsonNull }?.asString?.let { UUID.fromString(it) },
            uniqueId = obj["uuid"]?.takeIf { it !is JsonNull }?.asString?.let { UUID.fromString(it) },
            type = obj["type"].asString,
            exp = obj["exp"]?.asLong(0) ?: 0,
            active = obj["active"].asBoolean(false),
            tier = obj["tier"].asString,
            heldItem = obj["heldItem"]?.takeIf { it !is JsonNull }?.asString,
            candyUsed = obj["candyUsed"].asInt(0),
            skin = obj["skin"]?.takeIf { it !is JsonNull }?.asString,
        )

        private val petRarityOffset = mapOf(
            "COMMON" to 0,
            "UNCOMMON" to 6,
            "RARE" to 11,
            "EPIC" to 16,
            "LEGENDARY" to 20,
            "MYTHIC" to 20,
        )

        private val petLevels = listOf(
            100,
            110,
            120,
            130,
            145,
            160,
            175,
            190,
            210,
            230,
            250,
            275,
            300,
            330,
            360,
            400,
            440,
            490,
            540,
            600,
            660,
            730,
            800,
            880,
            960,
            1050,
            1150,
            1260,
            1380,
            1510,
            1650,
            1800,
            1960,
            2130,
            2310,
            2500,
            2700,
            2920,
            3160,
            3420,
            3700,
            4000,
            4350,
            4750,
            5200,
            5700,
            6300,
            7000,
            7800,
            8700,
            9700,
            10800,
            12000,
            13300,
            14700,
            16200,
            17800,
            19500,
            21300,
            23200,
            25200,
            27400,
            29800,
            32400,
            35200,
            38200,
            41400,
            44800,
            48400,
            52200,
            56200,
            60400,
            64800,
            69400,
            74200,
            79200,
            84700,
            90700,
            97200,
            104200,
            111700,
            119700,
            128200,
            137200,
            146700,
            156700,
            167700,
            179700,
            192700,
            206700,
            221700,
            237700,
            254700,
            272700,
            291700,
            311700,
            333700,
            357700,
            383700,
            411700,
            441700,
            476700,
            516700,
            561700,
            611700,
            666700,
            726700,
            791700,
            861700,
            936700,
            1016700,
            1101700,
            1191700,
            1286700,
            1386700,
            1496700,
            1616700,
            1746700,
            1886700,
        )
    }
}
