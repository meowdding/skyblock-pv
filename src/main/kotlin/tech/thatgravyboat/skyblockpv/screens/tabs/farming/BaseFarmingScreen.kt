package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockpv.api.GardenApi
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.GardenData
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration.Companion.seconds

abstract class BaseFarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("FARMING", gameProfile, profile) {

    var gardenData: Optional<GardenData>? = null
    private var isDoneLoading = false
    private var initedWithGardenData = false

    override val categories: List<Category> get() = FarmingCategories.entries

    init {
        if (profile != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val data = GardenApi.getGardenData(profile)
                gardenData = if (data.isCompletedExceptionally) {
                    Optional.empty()
                } else {
                    Optional.of(data.get())
                }

                isDoneLoading = true
                if (!initedWithGardenData) {
                    McClient.tell { rebuildWidgets() }
                }
            }
        }

        Scheduling.schedule(10.seconds) {
            if (gardenData == null) {
                McClient.tell { rebuildWidgets() }
            }
        }
    }

    override fun init() {
        if (gardenData != null) {
            initedWithGardenData = true
        }
        super.init()
    }

    fun isDoneLoading() = isDoneLoading
}

enum class FarmingCategories(val screen: KClass<out BaseFarmingScreen>, override val icon: ItemStack) : Category {
    MAIN(FarmingScreen::class, Items.WHEAT.defaultInstance)
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)

}
