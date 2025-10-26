package me.owdding.skyblockpv.feature

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.ProfileAPI
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.data.api.skills.combat.DungeonData
import me.owdding.skyblockpv.data.repo.CatacombsCodecs
import me.owdding.skyblockpv.utils.ChatUtils
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.fetchGameProfile
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.platform.name
import tech.thatgravyboat.skyblockapi.utils.extentions.capitalize
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover

@Module
object PartyFinderJoin {

    private val joinMessageRegex = "Party Finder > (?<username>.*) joined the(?: dungeon)? group!.*".toRegex()

    @Subscription
    fun onChat(event: ChatReceivedEvent.Pre) {
        if (!Config.partyFinderMessage) return

        joinMessageRegex.match(event.text, "username") { (username) ->
            if (username.equals(McPlayer.name, ignoreCase = true)) {
                return@match
            }

            fetchGameProfile(username) { profile ->
                if (profile == null) {
                    (+"messages.player_not_found").sendWithPrefix()
                } else if (!RepoAPI.isInitialized()) {
                    ChatUtils.chat(
                        """
                            §cThe external repo is not initialized.
                            §cThis can mean your network is blocking our domain or your internet is not working.
                            §cPlease try again later or check your network connection. If the problem persists, please report it on our Discord server with your full log.
                            """.trimIndent(),
                        )
                } else {
                    getDungeonData(profile)
                }
            }
        }
    }

    fun getDungeonData(gameProfile: GameProfile) {
        CoroutineScope(Dispatchers.IO).launch {
            val profiles = ProfileAPI.getProfiles(gameProfile)
            if (profiles.isEmpty()) {
                return@launch
            }
            val selected = profiles.find { it.selected } ?: return@launch

            CoroutineScope(Dispatchers.IO).launch {
                selected.dataFuture.whenComplete {
                        _, throwable ->
                    if (throwable != null) {
                        SkyBlockPv.error("Failed to load profile data for dungeon party finder join for: ${gameProfile.name}", throwable)
                    }

                    val dungeonData = selected.dungeonData
                    if (dungeonData == null) {
                        McClient.runNextTick {
                            "messages.no_dungeon_data".asTranslated(gameProfile.name).sendWithPrefix()
                        }
                        return@whenComplete
                    }

                    sendBasicInfo(gameProfile, dungeonData, selected)
                    sendEquipmentInfo(gameProfile, selected)

                    McClient.runNextTick {
                        "messages.party_finder_join".asTranslated(gameProfile.name).sendWithPrefix()
                    }
                }
            }
        }
    }

    private fun countRuns(completions: Map<String, Long>?) = completions?.filterKeys { it != "total" }?.values?.sum() ?: 0

    fun sendBasicInfo(gameProfile: GameProfile, dungeonData: DungeonData, profile: SkyBlockProfile) {
        val catacombsXp = dungeonData.dungeonTypes["catacombs"]?.experience ?: 0

        val (catacombsLevel, catacombsProgressToNext) = CatacombsCodecs.getLevelAndProgress(catacombsXp, Config.skillOverflow)
        val cataLevel: Float = catacombsLevel + catacombsProgressToNext
        val cataLevelFormatted = String.format("%.2f", cataLevel)

        val selectedClass = dungeonData.selectedClass
        val classXp = dungeonData.classExperience[selectedClass] ?: 0

        val (classLevelFloor, classProgressToNext) = CatacombsCodecs.getLevelAndProgress(classXp, Config.skillOverflow)
        val classLevel: Float = classLevelFloor + classProgressToNext
        val classLevelFormatted = String.format("%.2f", classLevel)

        val catacombsCompl = dungeonData.dungeonTypes["catacombs"]?.completions
        val masterModeCompl = dungeonData.dungeonTypes["master_catacombs"]?.completions

        val runCounts = (countRuns(catacombsCompl) + countRuns(masterModeCompl)).coerceAtLeast(1)

        val secrets = dungeonData.secrets
        val secretAverage = secrets.toFloat() / runCounts.toFloat()
        val secretAverageFormatted = String.format("%.1f", secretAverage)

        var catacombsFloorInfoComponents = dungeonData.dungeonTypes["catacombs"]?.floors?.map { (floorId, floorData) ->
            if (floorId == "total") return@map null
            Text.of("") {
                append("messages.dungeon_partyfinder.floor_info".asTranslated(floorId, floorData.completions, floorData.fastestTime))
            }
        }
        if (catacombsFloorInfoComponents.isNullOrEmpty()) {
            catacombsFloorInfoComponents = listOf(
                Text.of(" §aThis player hasn't completed any Catacombs floors yet :3")
            )
        }

        var mastermodeFloorInfoComponents = dungeonData.dungeonTypes["master_catacombs"]?.floors?.map { (floorId, floorData) ->
            if (floorId == "total") return@map null
            Text.of("") {
                append("messages.dungeon_partyfinder.floor_info".asTranslated(floorId, floorData.completions, floorData.fastestTime))
            }
        }
        if (mastermodeFloorInfoComponents.isNullOrEmpty()) {
            mastermodeFloorInfoComponents = listOf(
                Text.of(" §aThis player hasn't completed any Mastermode floors yet :3")
            )
        }

        McClient.runNextTick {
            "messages.dungeon_partyfinder.header".asTranslated(gameProfile.name).sendWithPrefix()
            "messages.dungeon_partyfinder.general_info_header".asTranslated().send()

            "messages.dungeon_partyfinder.level".asTranslated(cataLevelFormatted, selectedClass.capitalize(), classLevelFormatted).send()
            "messages.dungeon_partyfinder.secrets_found".asTranslated(secrets, secretAverageFormatted).send()

            Text.of("") {
                append("messages.dungeon_partyfinder.floor_info_header".asTranslated("§2Catacombs"))
                this.hover = Text.join(
                    Text.of("§2Catacombs §cFloors\n"),
                    catacombsFloorInfoComponents,
                    separator = Text.of("\n")
                )
            }.send()
            // When mastermode implementation :(
            // Text.of("") {
            //     append("messages.dungeon_partyfinder.floor_info_header".asTranslated("§cMastermode"))
            //     this.hover = Text.join(
            //         Text.of("§cMastermode Floors\n"),
            //         mastermodeFloorInfoComponents,
            //         separator = Text.of("\n")
            //     )
            // }.send()
        }
    }

    fun sendEquipmentInfo(gameProfile: GameProfile, profile: SkyBlockProfile) {
        val equippedArmor = profile.inventory?.armorItems?.inventory?.reversed() ?: List(4) { ItemStack.EMPTY }
        val equipment = profile.inventory?.equipmentItems?.inventory ?: List(4) { ItemStack.EMPTY }

        // VERY resource intensive, might limit what storages we should check :3
        val inventoryItems = profile.inventory?.getAllItems() ?: emptyList()

        val hasDungeonBreaker = inventoryItems.hasSkyBlockId("DUNGEONBREAKER")
        val hasInfiniteSuperboom = inventoryItems.hasSkyBlockId("INFINITE_SUPERBOOM_TNT")
        val hasInfileap = inventoryItems.hasSkyBlockId("INFINITE_SPIRIT_LEAP")
        val hasSpringBoots = inventoryItems.hasSkyBlockId("SPRING_BOOTS")

        McClient.runNextTick {
            "messages.dungeon_partyfinder.equipment_header".asTranslated().send()
            sendItemStackInfo(equippedArmor, gameProfile)
            sendItemStackInfo(equipment, gameProfile)
            Text.of("    ") {
                append("messages.dungeon_partyfinder.utilities_header".asTranslated())
                this.hover = Text.join(
                    "messages.dungeon_partyfinder.utilities_header".asTranslated(),
                    Text.of(""),
                    listOf(
                        "messages.dungeon_partyfinder.has_bonus_item_$hasDungeonBreaker".asTranslated("Dungeonbreaker"),
                        "messages.dungeon_partyfinder.has_bonus_item_$hasInfiniteSuperboom".asTranslated("Infinityboom TNT"),
                        "messages.dungeon_partyfinder.has_bonus_item_$hasInfileap".asTranslated("Infinileap"),
                        "messages.dungeon_partyfinder.has_bonus_item_$hasSpringBoots".asTranslated("Spring Boots"),
                    ),
                    separator = Text.of("\n"),
                )
            }.send()
        }
    }

    private fun sendItemStackInfo(list: List<ItemStack>, gameProfile: GameProfile) {
        list.forEach { itemStack ->
            if (itemStack.isEmpty) {
                Text.of("    ") {
                    append("messages.dungeon_partyfinder.empty_slot".asTranslated(gameProfile.name))
                }
            } else {
                Text.of("    ") {
                    append(itemStack.hoverName)
                    this.hover = Text.join(itemStack.hoverName, Text.of(""), itemStack.getLore(), separator = Text.of("\n"))
                }.send()
            }
        }
    }
}

fun List<ItemStack>.hasSkyBlockId(id: String): Boolean {
    return this.any { stack ->
        SkyBlockId.fromItem(stack)?.skyblockId == id
    }
}
