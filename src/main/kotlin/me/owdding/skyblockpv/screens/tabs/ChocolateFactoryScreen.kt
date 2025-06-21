package me.owdding.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutBuilder.Companion.setPos
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.shorten
import me.owdding.lib.extensions.toReadableString
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.CfData
import me.owdding.skyblockpv.data.api.RabbitEmployee
import me.owdding.skyblockpv.data.repo.CfCodecs
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.level.ItemLike
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.split
import java.time.Instant

class ChocolateFactoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("CHOCOLATE_FACTORY", gameProfile, profile) {

    override fun create(bg: DisplayWidget) {
        val cf = profile.chocolateFactoryData ?: return

        val employees = getEmployees(cf)
        val rarities = getRarities(cf, CfCodecs.data)
        val info = getInfo(cf, CfCodecs.data)
        val upgrades = getUpgrades(cf)

        PvLayouts.frame(bg.width, bg.height) {
            if (maxOf(employees.width, upgrades.width) + info.width + rarities.width + 6 > bg.width) {
                widget(
                    PvLayouts.vertical(3, 0.5f) {
                        widget(employees)
                        widget(upgrades)
                        widget(info)
                        widget(rarities)
                    }.asScrollable(bg.width, bg.height),
                )
            } else {
                horizontal(3, 0.5f) {
                    vertical(3, 0.5f) {
                        widget(employees)
                        widget(upgrades)
                    }
                    widget(info)
                    widget(rarities)
                }
            }
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)
    }

    private fun getUpgrades(cf: CfData) = PvWidgets.label(
        "Upgrades",
        buildList {
            createUpgradeItem(Items.COOKIE, "Click Upgrade", cf.clickUpgrades + 1) {
                add("Increases the amount of ") {
                    color = PvColors.GRAY
                    append("Chocolate ") {
                        color = PvColors.GOLD
                    }

                }
                add("you get per click.") {
                    color = PvColors.GRAY
                }
            }
            createUpgradeItem(Items.CLOCK, "Time Tower", cf.timeTower?.level ?: 0) {
                add("Increases your ") {
                    color = PvColors.GRAY
                    append("Chocolate Production ") {
                        color = PvColors.GOLD
                    }
                }
                add("for ") {
                    color = PvColors.GRAY
                    append("1h ") {
                        color = PvColors.GREEN
                    }
                    append("per charge.")
                }
                space()
                add("Charges: ") {
                    color = PvColors.GRAY
                    append("${cf.timeTower?.charges ?: 0}") {
                        color = PvColors.LIGHT_PURPLE
                    }
                    append("/") {
                        color = PvColors.GRAY
                    }
                    append("3") {
                        color = PvColors.LIGHT_PURPLE
                    }
                }
            }
            createUpgradeItem(Items.RABBIT_FOOT, "Rabbit Shrine", cf.rabbitRarityUpgrades) {
                add("Increases the chance of getting ") {
                    color = PvColors.GRAY
                }
                add("higher rarity rabbits ") {
                    color = PvColors.LIGHT_PURPLE
                    append("during ") {
                        color = PvColors.GRAY
                    }
                    append("Hoppity's Hunt") {
                        color = PvColors.LIGHT_PURPLE
                    }
                    append(".") {
                        color = PvColors.GRAY
                    }
                }
            }
            createUpgradeItem(SkullTextures.COACH_JACKRABBIT.skull.copy(), "Coach Jackrabbit", cf.chocolateMultiplierUpgrades) {
                add("Increases the amount of ") {
                    color = PvColors.GRAY
                }
                add("Chocolate ") {
                    color = PvColors.GOLD
                    append("you get per second.") {
                        color = PvColors.GRAY
                    }
                }
            }
        }.map { Displays.padding(2, Displays.item(it, showTooltip = true)) }.toRow(2).let {
            ExtraDisplays.inventoryBackground(4, 1, Displays.padding(2, it))
        }.asWidget(),
    )

    private fun MutableList<ItemStack>.createUpgradeItem(item: ItemStack, name: String, level: Int, tooltipBuilder: TooltipBuilder.() -> Unit) = add(
        item.apply {
            val lore = TooltipBuilder().apply(tooltipBuilder).build().split("\n")
            set(DataComponents.CUSTOM_NAME, Text.join(name, " $level") { italic = false; color = PvColors.LIGHT_PURPLE })
            set(DataComponents.LORE, ItemLore(lore, lore))
        },
    )

    private fun MutableList<ItemStack>.createUpgradeItem(base: ItemLike, name: String, level: Int, tooltipBuilder: TooltipBuilder.() -> Unit) =
        createUpgradeItem(ItemStack(base), name, level, tooltipBuilder)

    private fun getInfo(cf: CfData, data: CfCodecs.CfRepoData) = PvWidgets.label(
        "Information",
        PvLayouts.vertical {
            string("Chocolate: ") {
                color = PvColors.DARK_GRAY
                append(cf.chocolate.shorten()) {
                    color = PvColors.GOLD
                }
            }
            string("Total Chocolate: ") {
                color = PvColors.DARK_GRAY
                append(cf.totalChocolate.shorten()) {
                    color = PvColors.GOLD
                }
            }
            string("Chocolate since Prestige: ") {
                color = PvColors.DARK_GRAY
                append(cf.chocolateSincePrestige.shorten()) {
                    color = PvColors.GOLD
                }
            }
            if (cf.prestigeLevel != data.misc.maxPrestigeLevel) {
                val needed = (data.misc.chocolatePerPrestige[cf.prestigeLevel + 1] ?: 0) - cf.chocolateSincePrestige
                val string = "§aReady!".takeIf { needed <= 0 } ?: needed.shorten()
                string("Chocolate for next Prestige: ") {
                    color = PvColors.DARK_GRAY
                    append(string) {
                        color = PvColors.GOLD
                    }
                }
            }
            string("Prestige Level: ") {
                color = PvColors.DARK_GRAY
                append("${cf.prestigeLevel}") {
                    color = PvColors.LIGHT_PURPLE
                }
            }
            string("Barn Capacity: ") {
                color = PvColors.DARK_GRAY
                append("${cf.barnCapacity}") {
                    color = PvColors.GREEN
                }
            }
            string("Last Updated: ") {
                color = PvColors.DARK_GRAY
                append(Instant.ofEpochMilli(cf.lastUpdate).toReadableString())
            }

            display(
                ExtraDisplays.text(
                    Text.of("Hitman Slots: ") {
                        color = PvColors.DARK_GRAY
                        append("${cf.hitman?.slots ?: 0} Unlocked") {
                            color = PvColors.RED
                        }
                        append(" - ")
                        append("${cf.hitman?.uncollectedEggs ?: 0} Ready") {
                            color = PvColors.RED
                        }
                    },
                    shadow = false,
                ).withTooltip {
                    add("Paid: ") {
                        color = PvColors.GRAY
                        val paid = cf.hitman?.slots?.minus(1)?.takeIf { it >= 0 }?.let { data.hitmanCost[it] } ?: 0
                        val total = data.hitmanCost.last()
                        val percentage = paid.toDouble() / total * 100

                        append(paid.toFormattedString()) {
                            color = PvColors.GOLD
                        }
                        append("/")
                        append(total.shorten(2)) {
                            color = PvColors.GOLD
                        }
                        append(" (")
                        append("${percentage.round()}%") {
                            color = PvColors.GOLD
                        }
                        append(")")
                    }
                },
            )
        },
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    private fun getRarities(cf: CfData, data: CfCodecs.CfRepoData) = PvWidgets.label(
        "Rarities",
        data.rabbits.entries
            .reversed()
            .associateWith { repo -> cf.rabbits.entries.filter { repo.value.contains(it.key) } }
            .map { (repo, entries) ->
                val item = data.textures.find { it.id == repo.key.name }?.skull ?: Items.BARRIER.defaultInstance

                PvWidgets.iconNumberElement(item, Text.of("${entries.size}") { color = repo.key.color }).withTooltip {
                    add(repo.key.name) {
                        bold = true
                        color = repo.key.color
                        append(
                            Text.of("${entries.size}") {
                                color = repo.key.color
                                bold = false
                            }.wrap(" §7(", "§7)"),
                        )
                    }
                    add("Uniques: ") {
                        color = PvColors.GRAY
                        append("${entries.size}") {
                            color = repo.key.color
                        }
                    }
                    add("Total: ") {
                        color = PvColors.GRAY
                        append("${entries.sumOf { it.value }}") {
                            color = PvColors.GOLD
                        }
                    }
                }
            }.chunked(4).map { it.toRow(1) }.toColumn(1, Alignment.CENTER).asWidget(),
    )

    private fun getEmployees(cf: CfData) = CfCodecs.data.employees.map { repoEmployee ->
        val employee = cf.employees.find { it.id == repoEmployee.id } ?: RabbitEmployee(repoEmployee.id, 0)
        val item = (CfCodecs.data.textures.find { it.id == employee.id }?.skull ?: Items.BARRIER.defaultInstance).takeIf { employee.level > 0 }
            ?: Items.GRAY_DYE.defaultInstance

        PvWidgets.iconNumberElement(item, Text.of("${employee.level}") { color = employee.color }).withTooltip {
            add(repoEmployee.name) {
                bold = true
                color = employee.color

                append(
                    Text.of("${employee.level}") {
                        color = employee.color
                        bold = false
                    }.wrap(" §7(", "§7)"),
                )
            }
            space()
            add("Produces ") {
                color = PvColors.GRAY
                append("+${repoEmployee.getReward(employee.level).toFormattedString()} Chocolate") { color = PvColors.GOLD }
                append(" per second.") { color = PvColors.GRAY }
            }
        }
    }.chunked(4).map { it.toRow(1) }.toColumn(1, Alignment.CENTER).let { PvWidgets.label("Employees", it.asWidget()) }
}
