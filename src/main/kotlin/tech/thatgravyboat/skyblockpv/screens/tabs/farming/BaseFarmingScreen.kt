package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.GardenApi
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.GardenProfile
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration.Companion.seconds

abstract class BaseFarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("FARMING", gameProfile, profile) {

    var gardenProfile: Result<GardenProfile>? = null
    private var isDoneLoading = false
    private var initedWithGardenData = false

    override val categories: List<Category> get() = FarmingCategories.entries

    init {
        profile?.let { onProfileSwitch(it) }
    }

    override fun onProfileSwitch(profile: SkyBlockProfile) {
        gardenProfile = null
        initedWithGardenData = false
        isDoneLoading = false
        CoroutineScope(Dispatchers.IO).launch {
            val data = GardenApi.getGardenData(profile)
            gardenProfile = data

            isDoneLoading = true
            if (!initedWithGardenData) {
                McClient.tell { rebuildWidgets() }
            }
        }

        Scheduling.schedule(10.seconds) {
            if (gardenProfile == null) {
                McClient.tell { rebuildWidgets() }
            }
        }
    }

    override fun init() {
        if (gardenProfile != null) {
            initedWithGardenData = true
        }
        super.init()
    }

    protected fun <T> loading(
        value: T,
        loadingValue: T,
        errorValue: T,
    ): T {
        val data = gardenProfile
        return when {
            data == null -> loadingValue
            data.isFailure -> errorValue
            else -> value
        }
    }

    protected fun loadingComponent(
        message: Component,
        loadingMessage: Component = Text.of("Loading...") { this.color = TextColor.RED },
        errorMessage: Component = Text.of("Error!") { this.color = TextColor.RED },
    ): Component {
        return loading(message, loadingMessage, errorMessage)
    }

    protected fun createWidget(title: String, element: LayoutElement, padding: Int = 0) = LayoutBuild.vertical {
        widget(Utils.getTitleWidget(title, element.width + padding))
        widget(Utils.getMainContentWidget(element, element.width + padding))
    }
}

enum class FarmingCategories(val screen: KClass<out BaseFarmingScreen>, override val icon: ItemStack) : Category {
    MAIN(FarmingScreen::class, Items.WHEAT.defaultInstance)
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)

}
