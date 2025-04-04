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
import tech.thatgravyboat.skyblockpv.data.api.skills.farming.GardenProfile
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

    protected fun loading(
        data: Result<GardenProfile>?,
        onSuccess: (GardenProfile) -> Unit,
        loadingValue: () -> Unit,
        errorValue: () -> Unit,
    ) {
        return when {
            data == null -> loadingValue()
            data.isFailure -> errorValue()
            else -> onSuccess(data.getOrThrow())
        }
    }

    protected fun <T> loaded(
        data: Result<GardenProfile>? = gardenProfile,
        whileLoading: T,
        onError: T,
        onSuccess: (GardenProfile) -> T,
    ): T {
        return when {
            data == null -> whileLoading
            data.isFailure -> onError
            else -> onSuccess(data.getOrThrow())
        }
    }

    protected fun <T> loadingValue(
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
    protected val loadingMessage by lazy {  Text.of("Loading...") { this.color = TextColor.RED } }
    protected val errorMessage by lazy {  Text.of("Error...") { this.color = TextColor.RED } }


    protected fun loadingComponent(
        successMessage: Component,
        loadingMessage: Component = this.loadingMessage,
        errorMessage: Component = this.errorMessage,
    ): Component {
        return loadingValue(successMessage, loadingMessage, errorMessage)
    }

    protected fun loadingComponent(
        data: Result<GardenProfile>? = gardenProfile,
        loadingMessage: Component = this.loadingMessage,
        errorMessage: Component = this.errorMessage,
        successMessage: (GardenProfile) -> Component,
    ): Component {
        return loaded(data, loadingMessage, errorMessage, successMessage)
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
