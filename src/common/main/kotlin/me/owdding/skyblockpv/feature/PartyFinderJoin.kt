package me.owdding.skyblockpv.feature

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.ComponentFactory
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.ProfileAPI
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.data.api.skills.combat.DungeonData
import me.owdding.skyblockpv.data.repo.CatacombsCodecs
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.fetchGameProfile
import me.owdding.skyblockpv.utils.displays.InventoryTooltipComponent
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.minecraft.ui.GatherItemTooltipComponentsEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.platform.name
import tech.thatgravyboat.skyblockapi.utils.extentions.capitalize
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import java.util.*

@Module
object PartyFinderJoin {

    private val joinMessageRegex = "Party Finder > (?<username>.*) joined the(?: dungeon)? group!.*".toRegex()

    // TODO: Use (weak)IdentityHashMap in the future as ItemStacks dont implement hashCode/equals
    private val itemComponentMap = WeakHashMap<ItemStack, InventoryTooltipComponent>()

    @Subscription
    fun onChat(event: ChatReceivedEvent.Pre) {
        if (!Config.partyFinderMessage) return

        joinMessageRegex.match(event.text, "username") { (username) ->
            if (username.equals(McPlayer.name, ignoreCase = true)) {
                return@match
            }

            fetchGameProfile(username) { profile ->
                Utils.validateGameProfile(profile) {
                    getDungeonData(profile!!)
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

            selected.dataFuture.whenComplete { _, throwable ->
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

                McClient.runNextTick {
                    Text.multiline(
                        sendBasicInfo(gameProfile, dungeonData, selected),
                        sendEquipmentInfo(gameProfile, selected),
                    ).sendWithPrefix()
                    "messages.party_finder_join".asTranslated(gameProfile.name).sendWithPrefix()
                }
            }
        }
    }

    fun sendBasicInfo(gameProfile: GameProfile, dungeonData: DungeonData, profile: SkyBlockProfile) = ComponentFactory.multiline {
        val catacombsXp = dungeonData.dungeonTypes["catacombs"]?.experience ?: 0

        val (catacombsLevel, catacombsProgressToNext) = CatacombsCodecs.getLevelAndProgress(catacombsXp, Config.skillOverflow)
        val cataLevel: Float = catacombsLevel + catacombsProgressToNext
        val cataLevelFormatted = String.format("%.2f", cataLevel)

        val selectedClass = dungeonData.selectedClass
        val classXp = dungeonData.classExperience[selectedClass] ?: 0

        val (classLevelFloor, classProgressToNext) = CatacombsCodecs.getLevelAndProgress(classXp, Config.skillOverflow)
        val classLevel: Float = classLevelFloor + classProgressToNext
        val classLevelFormatted = String.format("%.2f", classLevel)

        val catacombsCompl = dungeonData.dungeonTypes["catacombs"]?.totalCompletions ?: 0
        val masterModeCompl = dungeonData.dungeonTypes["master_catacombs"]?.totalCompletions ?: 0

        val runCounts = (catacombsCompl + masterModeCompl).coerceAtLeast(1)

        val secrets = dungeonData.secrets
        val secretAverage = secrets.toFloat() / runCounts.toFloat()
        val secretAverageFormatted = String.format("%.1f", secretAverage)

        val catacombsFloorInfoComponents = dungeonData.dungeonTypes["catacombs"]?.floors?.mapNotNull { (floorId, floorData) ->
            if (floorId == "total") return@mapNotNull null
            "messages.dungeon_partyfinder.floor_info".asTranslated(floorId, floorData.completions, floorData.fastestTime, floorData.fastestTimeSplus)
        }.takeUnless { it.isNullOrEmpty() } ?: listOf(Text.of(" §aThis player hasn't completed any Catacombs floors yet"))

        val mastermodeFloorInfoComponents = dungeonData.dungeonTypes["master_catacombs"]?.floors?.mapNotNull { (floorId, floorData) ->
            if (floorId == "total") return@mapNotNull null
            "messages.dungeon_partyfinder.floor_info".asTranslated(floorId, floorData.completions, floorData.fastestTime, floorData.fastestTimeSplus)
        }.takeUnless { it.isNullOrEmpty() } ?: listOf(Text.of(" §aThis player hasn't completed any Mastermode floors yet"))


        component("messages.dungeon_partyfinder.header".asTranslated(gameProfile.name))
        component("messages.dungeon_partyfinder.general_info_header".asTranslated())

        component("messages.dungeon_partyfinder.level".asTranslated(cataLevelFormatted, selectedClass.capitalize(), classLevelFormatted))
        component("messages.dungeon_partyfinder.secrets_found".asTranslated(secrets, secretAverageFormatted))

        component("messages.dungeon_partyfinder.catacombs_floor_info_header".asTranslated()) {
            this.hover = Text.multiline(
                Text.of("§2Catacombs §cFloors\n"),
                catacombsFloorInfoComponents,
            )
        }

        component("messages.dungeon_partyfinder.mastermode_floor_info_header".asTranslated()) {
            this.hover = Text.multiline(
                Text.of("§6Mastermode §cFloors\n"),
                mastermodeFloorInfoComponents,
            )
        }
    }

    fun sendEquipmentInfo(gameProfile: GameProfile, profile: SkyBlockProfile) = ComponentFactory.multiline {
        val equippedArmor = profile.inventory?.armorItems?.inventory?.reversed() ?: List(4) { ItemStack.EMPTY }
        val equipment = profile.inventory?.equipmentItems?.inventory ?: List(4) { ItemStack.EMPTY }

        val inventoryItems = profile.inventory?.getAllItems() ?: emptyList()

        val hasDungeonBreaker = inventoryItems.hasSkyBlockId("DUNGEONBREAKER")
        val hasInfiniteSuperboom = inventoryItems.hasSkyBlockId("INFINITE_SUPERBOOM_TNT")
        val hasInfileap = inventoryItems.hasSkyBlockId("INFINITE_SPIRIT_LEAP")
        val hasSpringBoots = inventoryItems.hasSkyBlockId("SPRING_BOOTS")

        component("messages.dungeon_partyfinder.equipment_header".asTranslated()) {
            if (profile.inventory?.inventoryItems != null) {
                val inventoryItems = (profile.inventory?.inventoryItems?.inventory ?: List(36) { ItemStack.EMPTY }).chunked(9)
                val reorderedItems = (inventoryItems.drop(1) + inventoryItems.take(1)).flatten()

                val item = Items.BARRIER.defaultInstance
                item.set(DataComponents.CUSTOM_NAME, Text.of("Inventory") { italic = false })
                val hoverEvent = HoverEvent.ShowItem(item)

                itemComponentMap[hoverEvent.item()] = InventoryTooltipComponent(reorderedItems, 9)

                this.style = Style.EMPTY.withHoverEvent(hoverEvent)
            }
        }

        component(getItemStackComponent(equippedArmor, gameProfile))
        component(getItemStackComponent(equipment, gameProfile))

        string("    ") {
            append("messages.dungeon_partyfinder.utilities_header".asTranslated())
            this.hover = Text.multiline(
                "messages.dungeon_partyfinder.utilities_header".asTranslated(),
                CommonText.EMPTY,
                listOf(
                    "messages.dungeon_partyfinder.has_bonus_item_$hasDungeonBreaker".asTranslated("Dungeonbreaker"),
                    "messages.dungeon_partyfinder.has_bonus_item_$hasInfiniteSuperboom".asTranslated("Infinityboom TNT"),
                    "messages.dungeon_partyfinder.has_bonus_item_$hasInfileap".asTranslated("Infinileap"),
                    "messages.dungeon_partyfinder.has_bonus_item_$hasSpringBoots".asTranslated("Spring Boots"),
                ),
            )
        }
    }

    private fun getItemStackComponent(list: List<ItemStack>, gameProfile: GameProfile) = Text.multiline(
        list.map {
            Text.of("    ") {
                if (it.isEmpty) {
                    append("messages.dungeon_partyfinder.empty_slot".asTranslated(gameProfile.name))
                } else {
                    append(it.hoverName)
                    this.hover = Text.multiline(it.hoverName, CommonText.EMPTY, it.getLore())
                }
            }
        },
    )

    @Subscription
    private fun GatherItemTooltipComponentsEvent.onComponents() {
        itemComponentMap[this.item]?.let { components.add(it) }
    }

    private fun List<ItemStack>.hasSkyBlockId(id: String): Boolean = this.any { stack -> SkyBlockId.fromItem(stack)?.skyblockId == id }
}
