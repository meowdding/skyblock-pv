package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import com.notkamui.keval.keval
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.data.api.CfFaction
import me.owdding.skyblockpv.generated.CodecUtils
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.extentions.enumMapOf
import java.util.EnumMap

@Module
object CfCodecs {
    val data: CfRepoData by SkyBlockPv.repoDelegate("pv/chocolate_factory", CfRepoData())

    @IncludedCodec(named = "cf§texture_list")
    val CF_TEXTURE_CODEC: Codec<List<CfTextureRepo>> = CodecUtils.map<String, String>().xmap(
        { it.map { (key, value) -> CfTextureRepo(key, value) }.toList() },
        { it.associateTo(mutableMapOf()) { value -> value.id to value.texture } },
    )


    @GenerateCodec
    data class CfRepoData(
        @NamedCodec("cf§texture_list")
        val textures: List<CfTextureRepo> = emptyList(),
        val employees: List<CfEmployeeRepo> = emptyList(),
        val rabbits: EnumMap<SkyBlockRarity, List<String>> = enumMapOf(),
        val misc: CfMiscRepo = CfMiscRepo(),
        @NamedCodec("cum_long_list") @FieldName("hitman_cost")
        val hitmanCost: List<Long> = emptyList(),
        val factions: CfFactions = CfFactions()
    )

    @GenerateCodec
    data class CfFactions(
        val city: EnumMap<SkyBlockRarity, @Compact List<String>> = enumMapOf(),
        val country: EnumMap<SkyBlockRarity, @Compact List<String>> = enumMapOf(),
        val beach: EnumMap<SkyBlockRarity, @Compact List<String>> = enumMapOf(),
        val mountain: EnumMap<SkyBlockRarity, @Compact List<String>> = enumMapOf(),
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
        @FieldName("reward")
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

    @GenerateCodec
    data class CfMiscRepo(
        @NamedCodec("int_long_map") @FieldName("chocolate_prestige")
        val chocolatePerPrestige: Map<Int, Long> = emptyMap(),
    ) {
        val maxPrestigeLevel = chocolatePerPrestige.size + 1
    }
}

