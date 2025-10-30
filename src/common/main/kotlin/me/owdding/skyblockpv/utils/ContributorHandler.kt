package me.owdding.skyblockpv.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.cosmetics.CosmeticManager
import me.owdding.lib.events.CosmeticLoadEvent
import me.owdding.lib.extensions.associateNotNull
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs
import net.minecraft.core.ClientAsset
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.animal.Parrot
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import java.net.URI
import java.util.*

@Module
object ContributorHandler {
    var contributors: MutableMap<UUID, ContributorData> = mutableMapOf()
        private set

    val emptyData = ContributorData(null, null, null, false, null)

    val catResources: MutableMap<String, ResourceLocation> = mutableMapOf()

    @Subscription
    fun onCostmeticLoad(event: CosmeticLoadEvent) {
        catResources.clear()
        CoroutineScope(Dispatchers.IO).launch {
            val catCosmeticMap = CosmeticManager.cosmetics.values.mapNotNull { cosmetic ->
                val catTextureUrl = cosmetic.data.get("pv:cat_texture")?.asString
                if (!catTextureUrl.isNullOrEmpty()) {
                    cosmetic.id to catTextureUrl
                } else {
                    null
                }
            }.toMap()

            catCosmeticMap.forEach { (id, url) ->
                catResources[id] = CosmeticManager.imageProvider.get(URI(url))
            }

            val contributorData = CosmeticManager.playerList.associateNotNull(
                keySelector = { it.uuid },
                valueSelector = { playerEntry ->
                    val unmodifiedData = playerEntry.data.toData(SkyBlockPVCodecs.getCodec<ContributorData>()) ?: emptyData

                    val playerCosmetics = playerEntry.data.get("cosmetics")?.asJsonArray?.mapNotNull { it.asString } ?: emptyList()

                    val catCosmeticId = playerCosmetics.firstOrNull { it in catCosmeticMap }
                    val catResource = catCosmeticId?.let { catResources[it] }

                    var finalData = unmodifiedData

                    if (catResource != null && finalData.cat == null) {
                        val catMeow = CatOnShoulder(catResource, unmodifiedData.parrot?.leftSide == false)

                        finalData = finalData.copy(cat = catMeow)
                    }

                    finalData.takeUnless { it == emptyData }
                }
            )

            contributors.putAll(contributorData)
        }
    }
}

@GenerateCodec
data class ContributorData(
    @NamedCodec("component_tag") @FieldName("pv:title") val title: Component?,
    @FieldName("pv:parrot") val parrot: ParrotOnShoulder?,
    val cat: CatOnShoulder?,
    @FieldName("pv:shaking") val shaking: Boolean = false,
    @FieldName("pv:title_colors") val tileColors: List<TextColor>?,
) {
    val titleShader = tileColors?.let { GradientTextShader(*it.toTypedArray()) }
}

@GenerateCodec
data class ParrotOnShoulder(val variant: Parrot.Variant, @FieldName("left_shoulder") val leftSide: Boolean)

@GenerateCodec
data class CatOnShoulder(@FieldName("asset_id") val asset: ClientAsset, @FieldName("left_shoulder") val leftSide: Boolean)

/**
 * JSON CONTRIBUTOR REFERENCE, MORE CATS CAN BE ADDED, ONLY ONE CAT PER PLAYER
 * {"players":[{"uuid":"16102479-7162-4ea9-9975-a5059c6a2be3","extra_data":{"suffix":"§dᴍᴇᴏᴡ"},"cosmetics":["trans_cat"]},{"uuid":"503450fc-72c2-4e87-8243-94e264977437","extra_data":{"pv:title":"Super cool person","pv:title_colors":["#55CDFC","#F7A8B8","#FFFFFF","#F7A8B8","#55CDFC"],"suffix":"§dᴍᴇᴏᴡ"},"cosmetics":["trans_cape","trans_cat"]},{"uuid":"5c8c784b-e72f-4d90-beaa-8040b35940d5","extra_data":{},"cosmetics":["trans_cape"]},{"uuid":"a1732122-e22e-4edf-883c-09673eb55de8","extra_data":{"pv:parrot":{"left_shoulder":true,"variant":"blue"},"pv:title":"Cool person","pv:title_colors":["#55CDFC","#F7A8B8","#FFFFFF","#F7A8B8","#55CDFC"]},"cosmetics":["trans_cat","trans_cape"]},{"uuid":"add71246-c46e-455c-8345-c129ea6f146c","extra_data":{},"cosmetics":["pink_heart_cape"]},{"uuid":"b491990d-53fd-4c5f-a61e-19d58cc7eddf","extra_data":{},"cosmetics":["pink_heart_cape"]},{"uuid":"b75d7e0a-03d0-4c2a-ae47-809b6b808246","extra_data":{"pv:parrot":{"left_shoulder":false,"variant":"green"},"pv:title":"§dɢᴀʏ","suffix":"§dɢᴀʏ"},"cosmetics":["trans_cat","trans_cape"]},{"uuid":"bd082a55-373e-4305-bee3-48b9060e16c9","extra_data":{"pv:title":"Rift","pv:title_colors":["#2A0134","#CBC3E3","#FF0084","#2A0134"]},"cosmetics":[]},{"uuid":"e32bbc3d-f9a8-4e9a-b0a7-ae1d2de7bd9f","extra_data":{},"cosmetics":["trans_cape","trans_cat"]},{"uuid":"e90ea9ec-080a-401b-8d10-6a53c407ac53","extra_data":{"pv:parrot":{"left_shoulder":false,"variant":"red_blue"},"pv:title":"§dɢᴀʏ","suffix":"§dᴜᴡᴜ"},"cosmetics":["trans_cat","trans_cape"]},{"uuid":"ec7d1353-ebf0-4efa-a050-2af8c9d72a35","extra_data":{"pv:parrot":{"left_shoulder":false,"variant":"red_blue"}},"cosmetics":["trans_cape","trans_cat"]},{"uuid":"ecb01af3-ca09-4378-9d32-e71eac808c5a","extra_data":{},"cosmetics":["lesbian_cape"]},{"uuid":"ecdf4cc9-0487-4d6f-bf09-8497deaf8b33","extra_data":{},"cosmetics":["trans_cape","trans_cat"]}],"cosmetics":[{"cape_texture":"https://files.meowdd.ing/u/Z9Qfby.png","id":"trans_cape","version":1},{"cape_texture":"https://files.meowdd.ing/u/HBNSu7.png","id":"lesbian_cape","version":1},{"cape_texture":"https://files.meowdd.ing/u/qixuKv.png","id":"pink_heart_cape","version":1},{"id":"trans_cat","pv:cat_texture":"https://files.meowdd.ing/u/9rkJD9.png","version":1}]}
 */
