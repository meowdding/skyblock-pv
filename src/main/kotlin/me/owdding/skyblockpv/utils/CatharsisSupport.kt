package me.owdding.skyblockpv.utils

import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.BiConsumer

object CatharsisSupport {

    private var idConsumer: BiConsumer<ItemStack, Identifier> = BiConsumer { _, _ -> }
    private var disabledConsumer: BiConsumer<ItemStack, Boolean> = BiConsumer { _, _ -> }

    @JvmStatic
    fun id(consumer: BiConsumer<ItemStack, Identifier>) {
        this.idConsumer = consumer
    }

    @JvmStatic
    fun disabled(consumer: BiConsumer<ItemStack, Boolean>) {
        this.disabledConsumer = consumer
    }

    fun ItemStack.disableCatharsisModifications() = apply {
        disabledConsumer.accept(this, true)
    }

    fun ItemStack.withCatharsisId(path: String): ItemStack = apply {
        idConsumer.accept(this, SkyBlockPv.id(path))
    }

    fun Item.withCatharsisId(path: String): ItemStack = defaultInstance.apply {
        idConsumer.accept(this, SkyBlockPv.id(path))
    }
}

