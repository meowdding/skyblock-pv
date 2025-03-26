package tech.thatgravyboat.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.MiningGear
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets.armorAndEquipmentBuild
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets.toolsBuild

class MiningGearScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    override fun getLayout(): Layout {
        val profile = profile ?: return LayoutBuild.horizontal { }

        return LayoutBuild.horizontal(5, 0.5f) {
            armorAndEquipmentBuild(
                profile,
                ::calculateItemScore,
                MiningGear.necklaces,
                MiningGear.cloaks,
                MiningGear.belts,
                MiningGear.gloves,
                MiningGear.armor,
            )

            toolsBuild(
                profile,
                ::calculateItemScore,
                MiningGear.pickaxes,
                "icon/slot/pickaxe",
            )

            toolsBuild(
                profile,
                ::calculateItemScore,
                MiningGear.chisels,
                "icon/slot/armorstand",
                maxAmount = 1,
            )
        }.let {
            PvWidgets.label("Mining Gear", it, 20)
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
