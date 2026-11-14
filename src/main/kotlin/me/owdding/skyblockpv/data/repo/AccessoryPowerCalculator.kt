package me.owdding.skyblockpv.data.repo

import me.owdding.lib.repo.AccessoryFamily
import me.owdding.lib.repo.AccessoryRepoData
import me.owdding.lib.repo.AccessoryRepoData.isRiftPrism
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture

object AccessoryPowerCalculator {
    private data class Accessory(
        val item: ItemStack,
        val skyblockId: SkyBlockId?,
        val accessoryPower: Int,
    ) {
        val family: AccessoryFamily? = skyblockId?.let(AccessoryRepoData::getFamily)
    }

    // TODO: move to mlib, similar to the isRiftPrism function
    private const val HATCESSORY_FAMILY = "hatcessory"
    private const val ABICASE_FAMILY = "abicase"

    // TODO: move to repo
    private const val RIFT_PRISM_AP = 11

    fun calculateAccessoryPower(profile: SkyBlockProfile): CompletableFuture<Pair<Int, Component>> = profile.dataFuture.thenApplyAsync(
        {
            val items = profile.inventory?.talismans?.flatten().orEmpty()

            val base = items.associateWith(AccessoryRepoData::getAp).filterDuplicates()

            val consumedRiftPrism = profile.maxwell?.consumedRiftPrism == true
            if (consumedRiftPrism) base.removeIf { it.family?.isRiftPrism() == true }
            val riftPrismAp = if (consumedRiftPrism) RIFT_PRISM_AP else 0

            val hasAbicase = base.any { it.family?.family == ABICASE_FAMILY }
            val abicaseAp = if (hasAbicase) profile.maxwell?.abiphoneContacts?.floorDiv(2) ?: 0 else 0

            val lore = TooltipBuilder.multiline {
                add("Accessory Power Breakdown:", TextColor.GRAY)
                base.toList()
                    .sortedByDescending { it.item.getData(DataTypes.RARITY) }
                    .groupBy { it.item.getData(DataTypes.RARITY) }
                    .forEach { (rarity, items) ->
                        val rarity = rarity ?: return@forEach
                        val totalPower = items.sumOf { it.accessoryPower }
                        add {
                            color = TextColor.ORANGE

                            append(rarity.displayText)
                            append(": +${totalPower.toFormattedString()}")
                            append(" (${items.size})", TextColor.GRAY)
                        }
                    }

                space()
                add("Rift Prism:") {
                    color = TextColor.BLUE
                    append(" +$riftPrismAp", TextColor.GOLD)
                }
                add("Abiphone Case:") {
                    color = TextColor.BLUE
                    append(" +$abicaseAp", TextColor.GOLD)
                }

            }

            (base.sumOf { it.accessoryPower } + riftPrismAp + abicaseAp) to lore
        },
        Utils.executorPool,
    )

    private fun Map<ItemStack, Int>.filterDuplicates(): MutableList<Accessory> {
        val accessories = map { (item, ap) ->
            Accessory(item, item.getSkyBlockId(), ap)
        }.sortedByDescending { it.accessoryPower }.distinctBy { it.skyblockId }

        val currentIds = accessories.mapNotNullTo(mutableSetOf()) { it.skyblockId }

        val toRemove = accessories.filterTo(mutableSetOf()) { accessory ->
            val id = accessory.skyblockId ?: return@filterTo false // if it doesn't have an id, idfk
            val family = accessory.family ?: return@filterTo false // if we don't have a family, we don't remove just in case
            val tier = family[id] ?: return@filterTo false // this should never happen in theory
            if (tier.tier == family.maxTier) return@filterTo false // ignore if the tier is max
            val aboveTiers = family.tiers.subList(tier.tier + 1, family.size)
            // if any of the tiers above this one contain one of the accessories we have
            aboveTiers.any { aboveTier -> aboveTier.any { aboveId -> aboveId in currentIds } }
        }

        val hatItems = accessories.filter { it.family?.family == HATCESSORY_FAMILY }
        if (hatItems.size > 1) {
            val bestHat = hatItems.maxByOrNull { it.accessoryPower }
            hatItems.forEach { accessory ->
                if (accessory == bestHat) return@forEach
                toRemove.add(accessory)
            }
            toRemove.remove(bestHat)
        }

        return accessories.filterNotTo(mutableListOf()) { it in toRemove }
    }
}
