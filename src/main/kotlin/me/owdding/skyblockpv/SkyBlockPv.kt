package me.owdding.skyblockpv

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.serialization.Codec
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigUI
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import kotlinx.coroutines.runBlocking
import me.owdding.dfu.item.MeowddingItemDfu
import me.owdding.ktmodules.Module
import me.owdding.lib.compat.meowdding.MeowddingConfigTranslationChecker
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.lib.utils.MeowddingUpdateChecker
import me.owdding.lib.utils.isMeowddingMaintainer
import me.owdding.lib.utils.mod.MeowddingMod
import me.owdding.lib.utils.unsafeCast
import me.owdding.repo.RemoteRepo
import me.owdding.skyblockpv.api.PvAPI
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.config.DevConfig
import me.owdding.skyblockpv.config.THEME_RENDERER
import me.owdding.skyblockpv.config.ThemeRenderer
import me.owdding.skyblockpv.feature.PartyFinderJoin.getDungeonData
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.generated.SkyBlockPvExtraData
import me.owdding.skyblockpv.generated.SkyBlockPvModules
import me.owdding.skyblockpv.screens.DisplayTest
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.fetchGameProfile
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.components.PvToast
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.event.Event
import net.minecraft.core.HolderLookup
import net.minecraft.data.registries.VanillaRegistries
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnRepoStatus
import tech.thatgravyboat.skyblockapi.api.events.misc.LiteralCommandBuilder
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.repo.RepoEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.url
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KType

@Module
object SkyBlockPv : MeowddingMod("skyblockpv") {

    //~ if >= 26.3 'createLookup' -> 'createWorldLookup'
    val registryLookup: HolderLookup.Provider by lazy { VanillaRegistries.createWorldLookup() }
    private var meowddingRepo: Boolean = false
    private var apiRepo: Boolean = false

    var dataFailed: Boolean = false
    var hasNotifiedAboutFailure: Boolean = false

    override val RESOURCE_PATH: String = "skyblock-pv"

    const val DISCORD: String = "https://meowdd.ing/discord"
    val configDir: Path = McClient.config.resolve(MOD_ID)
    val userAgent: String = "SkyBlockPV $VERSION (${String(Base64.getDecoder().decode("Y29udGFjdEB0aGF0Z3Jhdnlib2F0LnRlY2g="))}})"

    private val configurator = Configurator(MOD_ID)
    val config by lazy { Config.register(configurator) }

    val isDevMode get() = McClient.isDev || DevConfig.devMode
    val isSuperUser by lazy { McPlayer.uuid.isMeowddingMaintainer() }

    val backgroundTexture = id("buttons/normal")

    fun ifDevMode(action: () -> Unit) {
        if (isDevMode) action()
    }

    override fun onInitializeClient() {
        config // used to instantiate the lazy :3
        ResourcefulConfigUI.registerElementRenderer(THEME_RENDERER, ::ThemeRenderer)

        if (isSuperUser) {
            MeowddingItemDfu.logErrors = true
        }

        registerEvents(SkyBlockPvModules.collected)
        MeowddingConfigTranslationChecker.addModToWarn(MOD_ID)

        MeowddingUpdateChecker("8yqXwFLl", SELF) { link, current, new ->
            fun MutableComponent.withLink() = this.apply {
                this.url = link
                this.hover = Text.of(link).withColor(TextColor.GRAY)
            }

            McClient.runNextTick {
                Text.of().send()
                "messages.new_version".asTranslated(current, new).withLink().sendWithPrefix()
                (+"messages.new_version.download").withLink().sendWithPrefix()
                Text.of().send()
            }
        }
        runBlocking { PvAPI.authenticate() }

        // Prioritise over skyblocker pv
        val commandId = id("skyblock_pv_command")
        ClientCommandRegistrationCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, commandId)
        ClientCommandRegistrationCallback.EVENT.register(commandId) { dispatcher, context ->
            if (!Config.isDisabled) {
                dispatcher.root.children.removeIf { it.name == "pv" }
                onRegisterCommands(RegisterCommandsEvent(dispatcher, context))
            }
        }


        apiRepo = RepoAPI.isInitialized()
        meowddingRepo = RemoteRepo.isInitialized()

        onRepoReady()
    }

    @OnRepoStatus(SUCCESS)
    @Subscription(RepoEvent.Reload::class)
    private fun onApiRepoReady() = onRepoReady()

    @Subscription(FinishRepoLoadingEvent::class)
    private fun onMeowddingRepoReady() = onRepoReady()

    private fun onRepoReady() {
        // both sbapi and meowdding repo need to be initialized
        if (!RepoAPI.isInitialized() || !RemoteRepo.isInitialized()) return
        SkyBlockPvExtraData.collected.forEach {
            CompletableFuture.supplyAsync {
                runBlocking { it.load() }
            }.exceptionally { throwable ->
                it.loadFallback()
                dataFailed = true
                warn("Loading fallback for ${it.javaClass.name}")
                McClient.runNextTick {
                    throw throwable
                }
            }
        }
    }

    @Subscription(ScreenInitializedEvent::class)
    private fun onScreenInitialized() {
        if (!dataFailed || hasNotifiedAboutFailure) return
        PvToast.addFailedToLoadDataToast()
        hasNotifiedAboutFailure = true
    }

    @Subscription
    private fun onRegisterCommands(event: RegisterCommandsEvent) {
        val pvCommand: (LiteralCommandBuilder.() -> Unit) = {
            callback {
                if (!PvAPI.isAuthenticated()) {
                    (+"messages.api.not_authenticated").sendWithPrefix()
                    return@callback
                }
                Utils.openPv(McClient.self.gameProfile)
            }
            then("player", StringArgumentType.string(), SkyBlockPlayerSuggestionProvider) {
                callback {
                    if (!PvAPI.isAuthenticated()) {
                        (+"messages.api.not_authenticated").sendWithPrefix()
                        return@callback
                    }
                    Utils.openPv(this.getArgument("player", String::class.java))
                }
            }
        }

        event.register("pv") { pvCommand() }
        event.register("sbpv") {
            then("pv") { pvCommand() }

            thenCallback("dev displaytest") {
                McClient.setScreenAsync { DisplayTest }
            }

            thenCallback("version") {
                Text.of("Version: $VERSION").withColor(TextColor.GRAY).sendWithPrefix()
            }

            thenCallback("discord") {
                Text.of("Join the Meowdding Discord!").apply {
                    this.url = DISCORD
                    this.hover = Text.of(DISCORD).withColor(TextColor.GRAY)
                }.sendWithPrefix()
            }

            thenCallback("dev pfjoin player", StringArgumentType.string(), SkyBlockPlayerSuggestionProvider) {
                fetchGameProfile(this.getArgument("player", String::class.java)) { profile ->
                    Utils.validateGameProfile(profile) {
                        getDungeonData(profile!!)
                    }
                }
            }

            callback {
                McClient.setScreenAsync { ResourcefulConfigScreen.getFactory(MOD_ID).apply(null) }
            }
        }
    }

    override fun <T : Any> getCodec(type: KType): Codec<T> = SkyBlockPvCodecs.getCodec(type).unsafeCast()

    //? if < 26.4
    @Deprecated("Use getCodec(KType) instead. This will be removed on 26.4") override fun <T : Any> getCodec(clazz: Class<T>): Codec<T> = SkyBlockPvCodecs.getCodec(clazz).unsafeCast()

    fun olympusId(path: String): Identifier = Identifier.fromNamespaceAndPath("olympus", path)
}
