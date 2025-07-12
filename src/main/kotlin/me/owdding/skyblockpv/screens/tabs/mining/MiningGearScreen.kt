package me.owdding.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.MiningGear
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

class MiningGearScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    override fun getLayout(bg: DisplayWidget): Layout {
        return PvLayouts.horizontal(5, 0.5f) {
            widget(
                PvWidgets.armorAndEquipment(
                    profile,
                    ::calculateItemScore,
                    MiningGear.necklaces,
                    MiningGear.cloaks,
                    MiningGear.belts,
                    MiningGear.gloves,
                    MiningGear.armor,
                ),
            )

            widget(
                PvWidgets.tools(
                    profile,
                    ::calculateItemScore,
                    MiningGear.pickaxes,
                    "icon/slot/pickaxe",
                ),
            )

            vertical(5) {
                widget(
                    PvWidgets.tools(
                        profile,
                        ::calculateItemScore,
                        MiningGear.chisels,
                        "icon/slot/armorstand",
                        maxAmount = 1,
                    ),
                )

                val suspiciousScrapId = MiningGear.suspicious_scrap.first()
                val scrapItem = RepoItemsAPI.getItem(suspiciousScrapId)
                var scrapsCount = profile.inventory?.getAllItems()?.filter {
                    it.getData(DataTypes.ID) == suspiciousScrapId
                }?.sumOf { it.count } ?: 0
                profile.inventory?.sacks[suspiciousScrapId]?.let { amount ->
                    scrapsCount += amount.toInt()
                }
                val display = ExtraDisplays.inventorySlot(
                    Displays.padding(
                        4,
                        Displays.item(scrapItem, customStackText = scrapsCount, showTooltip = true),
                    ),
                )
                display(display)
            }

        }.let {
            PvWidgets.label("Mining Gear", it)
        }
    }


    private fun calculateItemScore(itemStack: ItemStack): Int {
        fun <T> getData(type: DataType<T>): T? = itemStack.getData(type)

        var score = 0

        score += 1.takeIf { getData(DataTypes.RECOMBOBULATOR) ?: false } ?: 0

        // take the actual level of ultimate enchants since those are worth smth
        getData(DataTypes.ENCHANTMENTS)?.let {
            score += it.keys.firstOrNull { key -> key.startsWith("ultimate") }?.let { key -> it[key] } ?: 0
        }

        // only counting t8 and above, since t7 are just 64 t1s, maybe this still has to be tweaked
        score += getData(DataTypes.ATTRIBUTES)?.map { it.value - 7 }?.filter { it > 0 }?.sum() ?: 0

        // only counting t5 and above
        score += getData(DataTypes.ENCHANTMENTS)?.map { it.value - 4 }?.filter { it > 0 }?.sum() ?: 0

        score += getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        score += ((getData(DataTypes.RARITY)?.ordinal ?: 0) - 2).coerceIn(0, 3)

        score += getData(DataTypes.FUEL_TANK)?.let { 1 } ?: 0
        score += getData(DataTypes.ENGINE)?.let { 1 } ?: 0
        score += getData(DataTypes.UPGRADE_MODULE)?.let { 1 } ?: 0

        return score
    }
}
