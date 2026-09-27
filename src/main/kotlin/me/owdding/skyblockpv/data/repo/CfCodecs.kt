package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import com.notkamui.keval.keval
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.skyblockpv.data.api.CfFaction
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

@LoadData
object CfCodecs : DefaultedData {
    private val defaultData = CfRepoData(
        emptyList(),
        emptyList(),
        emptyMap(),
        CfMiscRepo(emptyMap()),
        emptyList(),
        CfFactions(
            emptyMap(),
            emptyMap(),
            emptyMap(),
            emptyMap(),
        )
    )
    private var _data: CfRepoData? = null
    val data: CfRepoData get() = _data ?: defaultData

    @IncludedCodec(named = "cf§texture_list")
    val CF_TEXTURE_CODEC: Codec<List<CfTextureRepo>> = CodecUtils.map<String, String>().xmap(
        { it.map { (key, value) -> CfTextureRepo(key, value) }.toList() },
        { it.associate { value -> value.id to value.texture } },
    )

    override suspend fun load() {
        _data = Utils.loadRemoteRepoData<CfRepoData>("pv/chocolate_factory")
    }


    @GenerateCodec
    data class CfRepoData(
        @NamedCodec("cf§texture_list") val textures: List<CfTextureRepo>,
        val employees: List<CfEmployeeRepo>,
        val rabbits: Map<SkyBlockRarity, List<String>>,
        val misc: CfMiscRepo,
        @NamedCodec("cum_long_list") @FieldName("hitman_cost") val hitmanCost: List<Long>,
        val factions: CfFactions
    )

    @GenerateCodec
    data class CfFactions(
        val city: Map<SkyBlockRarity, @Compact List<String>>,
        val country: Map<SkyBlockRarity, @Compact List<String>>,
        val beach: Map<SkyBlockRarity, @Compact List<String>>,
        val mountain: Map<SkyBlockRarity, @Compact List<String>>,
    ) {
        fun select(faction: CfFaction) = when (faction) {
            CfFaction.CITY -> city
            CfFaction.MOUNTAIN -> mountain
            CfFaction.COUNTRY -> country
            CfFaction.BEACH -> beach
        }
    }

    @GenerateCodec
    data class CfEmployeeRepo(
        val id: String,
        val name: String,
        @FieldName("reward") val rewardFormula: String,
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

    @GenerateCodec
    data class CfMiscRepo(
        @NamedCodec("int_long_map") @FieldName("chocolate_prestige") val chocolatePerPrestige: Map<Int, Long>,
    ) {
        val maxPrestigeLevel = chocolatePerPrestige.size + 1
    }
}

