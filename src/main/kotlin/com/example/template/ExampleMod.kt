package com.example.template

import com.mojang.brigadier.CommandDispatcher
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.CommandBuildContext

object ExampleMod : ClientModInitializer {

    val configurator = Configurator("examplemod")

    override fun onInitializeClient() {
        configurator.register(ExampleConfig::class.java)
        ClientCommandRegistrationCallback.EVENT.register(::onRegisterCommands)
    }

    private fun onRegisterCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        buildContext: CommandBuildContext
    ) {
        dispatcher.register(
            ClientCommandManager.literal("yourcommand").executes { context ->
                println("Hello, world!")
                1
            })
    }
}