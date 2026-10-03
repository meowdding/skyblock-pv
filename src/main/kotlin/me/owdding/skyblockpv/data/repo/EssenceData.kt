package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.ktcodecs.Inline
import me.owdding.ktcodecs.OptionalNullable
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.generated.DispatchHelper
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.displays.withTranslatedTooltip
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.createSkull
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.reflect.KClass

@GenerateDispatchCodec(EssenceStack::class, typeKey = "display")
enum class EssenceStackType(override val type: KClass<out EssenceStack>) : DispatchHelper<EssenceStack> {
    SBID(SbIdEssenceStack::class),
    VANILLA(VanillaEssenceStack::class),
    POTION(PotionEssenceStack::class),
    SKULL(SkullEssenceStack::class),
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}

interface EssenceStack {
    val display: EssenceStackType

    fun getStack(): ItemStack
}

@GenerateCodec
data class SbIdEssenceStack(val sbid: SkyBlockId) : EssenceStack {
    override val display: EssenceStackType = EssenceStackType.SBID

    override fun getStack(): ItemStack = sbid.toItem()
}

@GenerateCodec
data class VanillaEssenceStack(val item: Identifier) : EssenceStack {
    override val display: EssenceStackType = EssenceStackType.VANILLA

    override fun getStack(): ItemStack = Utils.getMinecraftItem(item)
}

@GenerateCodec
data class PotionEssenceStack(@FieldName("potion_contents") val potionContents: PotionContents) : EssenceStack {
    override val display: EssenceStackType = EssenceStackType.POTION

    override fun getStack(): ItemStack = Items.POTION.defaultInstance.apply {
        set(DataComponents.POTION_CONTENTS, potionContents)
    }
}

@GenerateCodec
data class SkullEssenceStack(val texture: String) : EssenceStack {
    override val display: EssenceStackType = EssenceStackType.SKULL

    override fun getStack(): ItemStack = createSkull(texture)
}

@GenerateCodec
data class RepoEssencePerk(
    val name: String,
    @FieldName("max_level") val maxLevel: Int,
    @Inline @OptionalNullable val display: EssenceStack?,
)

@LoadData
object EssenceData : DefaultedData {
    val allPerks: MutableMap<String, RepoEssencePerk> = mutableMapOf()

    fun LayoutBuilder.addFishingPerk(profile: SkyBlockProfile, id: String) {
        addPerk(profile, id, "fishing")
    }

    fun LayoutBuilder.addMiningPerk(profile: SkyBlockProfile, id: String) {
        addPerk(profile, id, "mining")
    }

    fun LayoutBuilder.addSafariPerk(profile: SkyBlockProfile, id: String) {
        addPerk(profile, id, "safari", true)
    }

    fun LayoutBuilder.addPerk(profile: SkyBlockProfile, id: String, category: String, showItemStack: Boolean = false) {
        val perkLevel = profile.essenceUpgrades[id] ?: 0
        val perk = allPerks.entries.find { it.key == id }?.value
        val maxLevel = perk?.maxLevel ?: 0
        val isMaxed = perkLevel == maxLevel

        val name = Text.join(
            perk?.name ?: "Unknown",
            ": ",
            Text.of("$perkLevel", if (isMaxed) PvColors.GREEN else PvColors.RED),
            "/$maxLevel",
        )

        if (showItemStack && perk?.display != null) {
            val stackSize = Text.of(perkLevel.toString(), if (isMaxed) PvColors.GOLD else PvColors.RED)
            val itemDisplay = Displays.padding(2, Displays.item(perk.display.getStack(), customStackText = stackSize))
            val display = ExtraDisplays.inventorySlot(itemDisplay).withTooltip {
                add {
                    color = TextColor.GREEN
                    append(name)
                }
                add(Text.translatable("skyblockpv.essence.$category.$id"))
            }
            display(display)
        } else {
            val display = ExtraDisplays.text(name, { PvColors.DARK_GRAY.toUInt() }, false)
            display(display.withTranslatedTooltip("skyblockpv.essence.$category.$id"))
        }
    }

    override suspend fun load() {
        allPerks.putAll(
            Utils.loadRemoteRepoData("pv/essence_perks", Codec.unboundedMap(Codec.STRING, CodecUtils.map<String, RepoEssencePerk>()))
                .flatMap { it.value.entries }.associateBy({ it.key }, { it.value }),
        )
    }
}
