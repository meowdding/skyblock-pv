package tech.thatgravyboat.skyblockpv.screens.tabs.fishing

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.ItemApi
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.or
import tech.thatgravyboat.skyblockpv.data.FishingEquipment
import tech.thatgravyboat.skyblockpv.data.TrophyFish
import tech.thatgravyboat.skyblockpv.data.TrophyFishTiers
import tech.thatgravyboat.skyblockpv.data.TrophyFishTypes
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.Utils.getMainContentWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.getTitleWidget
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.math.RoundingMode
import java.text.DecimalFormat

class TrophyFishingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BasePvScreen("Fishing", gameProfile, profile) {

    private val numberFormatInstance = DecimalFormat.getCompactNumberInstance().apply {
        this.roundingMode = RoundingMode.FLOOR
        this.maximumFractionDigits = 1
    }

    override fun create(bg: DisplayWidget) {
        val profile = profile ?: return

        val trophyTable = TrophyFishTypes.entries.map { type ->
            getElement(type, profile)
        }.swapAxis().asTable(4).centerIn(uiWidth, -1).asWidget()

        LayoutBuild.vertical {
            spacer(height = 5)
            widget(
                LayoutBuild.horizontal {
                    spacer(height = (uiHeight * 0.5).toInt())
                    widget(getInfoElement(profile, (uiWidth * 0.2).toInt()))
                    widget(getGearWidget(profile, (uiWidth * 0.2).toInt()))
                },
            )
            widget(
                LayoutBuild.vertical {
                    spacer(height = 5)
                    widget(getTitleWidget("Trophy Fish", uiWidth))
                    widget(getMainContentWidget(trophyTable, uiWidth))
                },
            )
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)
    }

    private fun getGearWidget(profile: SkyBlockProfile, width: Int) = LayoutBuild.vertical {
        widget(getTitleWidget("Gear", width))

        LayoutBuild.horizontal {
            widget(getTrophyArmor(profile))
            spacer(width = 5)
            widget(getArmorAndEquipment(profile))
            spacer(width = 5)
            widget(getRods(profile))
        }.let { widget(getMainContentWidget(it, width)) }
    }

    private fun getTrophyArmor(profile: SkyBlockProfile): LayoutElement {
        val trophyArmor = ItemApi.getItemsMatching(
            profile,
            ItemApi.anySkyblockId(FishingEquipment.trophyArmor),
        ) ?: emptyList()

        val displayArmor = getDisplayArmor(trophyArmor).map { Displays.padding(2, Displays.item(it, showTooltip = true)) }.toColumn()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-1x4"),
            Displays.padding(2, displayArmor)
        ).asWidget()
    }

    private fun getDisplayArmor(list: List<ItemStack>) = buildList<ItemStack> {
        fun addArmor(type: String) {
            add(list.firstOrNull { it.getData(DataTypes.ID)?.contains(type) ?: false } ?: ItemStack.EMPTY)
        }

        addArmor("HELMET")
        addArmor("CHEST")
        addArmor("LEGGINGS")
        addArmor("BOOTS")
    }

    private fun getArmorAndEquipment(profile: SkyBlockProfile): LayoutElement {
        val armorAndEquipment = ItemApi.getItemsMatching(
            profile,
            ItemApi.anySkyblockId(FishingEquipment.armor).or(ItemApi.anySkyblockId(FishingEquipment.equipment)),
        )?.sortedBy(::calculateItemScore)?.reversed() ?: emptyList()

        val displayArmor = getDisplayArmor(armorAndEquipment)

        val displayEquipment = buildList<ItemStack> {
            fun addEquipment(type: FishingEquipment) {
                add(
                    armorAndEquipment.firstOrNull() {
                        it.getData(DataTypes.ID)?.let { id ->
                            type.list.contains(id)
                        } ?: false
                    } ?: ItemStack.EMPTY,
                )
            }

            addEquipment(FishingEquipment.NECKLACES)
            addEquipment(FishingEquipment.CLOAKS)
            addEquipment(FishingEquipment.BELTS)
            addEquipment(FishingEquipment.GLOVES)
        }

        val armorEquipment = listOf(
            displayArmor.map { Displays.padding(2, Displays.item(it, showTooltip = true)) }.toColumn(),
            displayEquipment.map { Displays.padding(2, Displays.item(it, showTooltip = true)) }.toColumn(),
        ).toRow()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-2x4"),
            Displays.padding(2, armorEquipment),
        ).centerIn(-1, -1).asWidget()
    }

    private fun getRods(profile: SkyBlockProfile): LayoutElement {
        val fishingRods = ItemApi.getItemsMatching(
            profile = profile,
            predicate = ItemApi.anySkyblockId(FishingEquipment.rods),
        )?.sortedBy(::calculateItemScore)?.reversed()?.take(4)?.toMutableList() ?: mutableListOf()

        if (fishingRods.size != 4) {
            for (i in (fishingRods.size - 1)..3) {
                fishingRods.add(i, Items.AIR.defaultInstance)
            }
        }

        val column = fishingRods.map { Displays.padding(2, Displays.item(it, showTooltip = true)) }.toColumn()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-1x4"),
            Displays.padding(2, column),
        ).centerIn(-1, -1).asWidget()
    }

    /**
     * Creates a score for a rod to determine which ones to display
     */
    private fun calculateItemScore(itemStack: ItemStack): Int {
        var score = 0

        // TODO add attributes to calculation after skyblockapi #55 is merged
        score += itemStack.getData(DataTypes.RARITY_UPGRADES) ?: 0

        // take the actual level of ultimate enchants since those are worth smth
        itemStack.getData(DataTypes.ENCHANTMENTS)?.let {
            score += it.keys.filter { key -> key.startsWith("ultimate") }.firstOrNull()?.let { key -> it[key] } ?: 0
        }

        // only counting t5 and t6 enchants as everything else is kinda useless
        score += itemStack.getData(DataTypes.ENCHANTMENTS)?.map { it.value - 4 }?.filter { it > 0 }?.sum() ?: 0

        score += itemStack.getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        score += ((itemStack.getData(DataTypes.RARITY)?.ordinal ?: 0) - 2).coerceIn(0, 3)


        return score
    }


    private fun getInfoElement(profile: SkyBlockProfile, width: Int) = LayoutBuild.vertical {
        widget(getTitleWidget("Information", width))
        widget(
            getMainContentWidget(
                LayoutBuild.vertical {
                    fun grayText(text: String) = display(Displays.text(text, color = { 0x555555u }, shadow = false))

                    grayText("Total Caught: ${profile.trophyFish.totalCatches}")
                },
                width,
            ),
        )
    }

    private fun getElement(types: TrophyFishTypes, profile: SkyBlockProfile): List<Display> {
        val fishies = TrophyFishTiers.entries.reversed().map { tiers -> TrophyFish(types, tiers) }
        val caught = fishies.associate {
            it.tier to profile.trophyFish.obtainedTypes.getOrDefault(it.apiName, 0)
        }

        val caughtTooltip = TrophyFishTiers.entries.map { tiers ->
            Text.of(tiers.displayName).append(": ").append("${caught[tiers] ?: 0}")
        }

        return fishies.map {
            getElement(it, profile, caught[it.tier] ?: 0).withTooltip(
                it.displayName,
                caughtTooltip,
            )
        }
    }

    private fun getElement(trophyFish: TrophyFish, profile: SkyBlockProfile, amountCaught: Int): Display {

        val item = if (!profile.trophyFish.obtainedTypes.containsKey(trophyFish.apiName)) {
            Displays.item(Items.GRAY_DYE.defaultInstance)
        } else {
            Displays.item(
                trophyFish.item,
                customStackText = numberFormatInstance.format(amountCaught),
            )
        }


        return Displays.background(
            SkyBlockPv.id("inventory/inventory-1x1"),
            Displays.padding(3, item),
        ).centerIn(-1, -1)
    }
}
