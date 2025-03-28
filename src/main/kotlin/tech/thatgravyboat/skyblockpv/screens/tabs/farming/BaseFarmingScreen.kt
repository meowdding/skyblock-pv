package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.GardenProfile
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration.Companion.seconds

// todo create abstraction for the loading methods
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
            val data = GardenApi.getData(profile)
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

    protected fun <D> loading(
        data: Result<D>?,
        onSuccess: (D) -> Unit,
        loadingValue: () -> Unit,
        errorValue: () -> Unit,
    ) {
        return when {
            data == null -> loadingValue()
            data.isFailure -> errorValue()
            else -> onSuccess(data.getOrThrow())
        }
    }

    protected fun <T, D> loading(
        data: Result<D>?,
        onSuccess: (D) -> T,
        whileLoading: T,
        onError: T,
    ): T {
        return when {
            data == null -> whileLoading
            data.isFailure -> onError
            else -> onSuccess(data.getOrThrow())
        }
    }

    protected fun <T> loading(
        successValue: T,
        loadingValue: T,
        errorValue: T,
    ): T {
        val data = gardenProfile
        return when {
            data == null -> loadingValue
            data.isFailure -> errorValue
            else -> successValue
        }
    }

    protected fun loadingComponent(
        successMessage: Component,
        loadingMessage: Component = loadingMessage(),
        errorMessage: Component = errorMessage(),
    ): Component {
        return loading(successMessage, loadingMessage, errorMessage)
    }

    protected fun loadingMessage() = Text.of("Loading...") { this.color = TextColor.RED }
    protected fun errorMessage() = Text.of("Error!") { this.color = TextColor.RED }

    protected fun <D> loadingComponent(
        data: Result<D>?,
        successMessage: (D) -> Component,
        loadingMessage: Component = loadingMessage(),
        errorMessage: Component = errorMessage(),
    ): Component {
        return loading(data, successMessage, loadingMessage, errorMessage)
    }
}

enum class FarmingCategories(val screen: KClass<out BaseFarmingScreen>, override val icon: ItemStack) : Category {
    MAIN(FarmingScreen::class, Items.WHEAT.defaultInstance),
    VISITORS(VisitorScreen::class, Items.VILLAGER_SPAWN_EGG.defaultInstance),
    CROP(CropScreen::class, Items.CARROT.defaultInstance),
    COMPOSTER(ComposterScreen::class, ItemAPI.getItem("COMPOST")),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)

}
