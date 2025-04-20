package me.owdding.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.notkamui.keval.keval
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.extentions.ItemUtils.createSkull
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData

@Module
object CfCodecs {
    var data: CfRepoData
        private set

    private val CfTextureCodec = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(
        { it.entries.map { (key, value) -> CfTextureRepo(key, value) } },
        { emptyMap() },
    )

    private val CfEmployeeCodec = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("id").forGetter(CfEmployeeRepo::id),
            Codec.STRING.fieldOf("name").forGetter(CfEmployeeRepo::name),
            Codec.STRING.fieldOf("reward").forGetter(CfEmployeeRepo::rewardFormula),
        ).apply(it, ::CfEmployeeRepo)
    }

    private val CfMiscCodec = RecordCodecBuilder.create {
        it.group(
            CodecUtils.INT_LONG_MAP.fieldOf("chocolate_prestige").forGetter(CfMiscRepo::chocolatePerPrestige),
        ).apply(it, ::CfMiscRepo)
    }

    private val CfRabbitRaritiesCodec = Codec.unboundedMap(CodecUtils.SKYBLOCK_RARITY_CODEC, Codec.STRING.listOf())

    init {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                CfTextureCodec.fieldOf("textures").forGetter(CfRepoData::textures),
                CfEmployeeCodec.listOf().fieldOf("employees").forGetter(CfRepoData::employees),
                CfRabbitRaritiesCodec.fieldOf("rabbits").forGetter(CfRepoData::rabbits),
                CfMiscCodec.fieldOf("misc").forGetter(CfRepoData::misc),
                CodecUtils.CUMULATIVE_LONG_LIST.fieldOf("hitman_cost").forGetter(CfRepoData::hitmanCost),
            ).apply(it, ::CfRepoData)
        }

        data = Utils.loadFromRepo<JsonObject>("chocolate_factory").toData(CODEC) ?: throw IllegalStateException("Failed to load chocolate factory data!")
    }

    data class CfRepoData(
        val textures: List<CfTextureRepo>,
        val employees: List<CfEmployeeRepo>,
        val rabbits: Map<SkyBlockRarity, List<String>>,
        val misc: CfMiscRepo,
        val hitmanCost: List<Long>,
    )

    data class CfEmployeeRepo(
        val id: String,
        val name: String,
        val rewardFormula: String,
    ) {
        fun getReward(level: Int) = rewardFormula.keval {
            includeDefault()
            constant {
                name = "level"
                value = level.toDouble()
            }
        }.toInt()
    }

    data class CfTextureRepo(
        val id: String,
        val texture: String,
    ) {
        val skull by lazy { createSkull(texture) }
    }

    data class CfMiscRepo(
        val chocolatePerPrestige: Map<Int, Long>,
    ) {
        val maxPrestigeLevel = chocolatePerPrestige.size + 1
    }
}

