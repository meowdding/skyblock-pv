package tech.thatgravyboat.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.lib.builder.LayoutBuild
import tech.thatgravyboat.lib.displays.DisplayWidget
import tech.thatgravyboat.lib.displays.Displays
import tech.thatgravyboat.skyblockapi.api.datatype.DataType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.api.skills.MiningGear
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.ExtraDisplays

class MiningGearScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    override fun getLayout(bg: DisplayWidget): Layout {
        val profile = profile ?: return LayoutBuild.horizontal { }

        return LayoutBuild.horizontal(5, 0.5f) {
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
                val scrapsCount = profile.inventory?.getAllItems()?.filter {
                    it.getData(DataTypes.ID) == suspiciousScrapId
                }?.sumOf { it.count } ?: 0
                val display = ExtraDisplays.inventorySlot(
                    Displays.padding(
                        2,
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

        score += getData(DataTypes.RARITY_UPGRADES) ?: 0

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
