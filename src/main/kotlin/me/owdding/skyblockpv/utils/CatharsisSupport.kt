package me.owdding.skyblockpv.utils

import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.BiConsumer

object CatharsisSupport {

    private var consumer: BiConsumer<ItemStack, Identifier> = BiConsumer { _, _ -> }

    @JvmStatic
    fun register(consumer: BiConsumer<ItemStack, Identifier>) {
        this.consumer = consumer
    }

    fun ItemStack.withCatharsisId(path: String): ItemStack = apply {
        consumer.accept(this, SkyBlockPv.id(path))
    }

    fun Item.withCatharsisId(path: String): ItemStack = defaultInstance.apply {
        consumer.accept(this, SkyBlockPv.id(path))
    }
}
