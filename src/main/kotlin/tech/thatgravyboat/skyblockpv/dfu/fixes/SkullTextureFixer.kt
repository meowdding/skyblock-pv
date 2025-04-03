package tech.thatgravyboat.skyblockpv.dfu.fixes

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.ResolvableProfile
import tech.thatgravyboat.skyblockapi.utils.extentions.getStringOrNull
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer
import java.util.*
import kotlin.jvm.optionals.getOrNull

object SkullTextureFixer : DataComponentFixer<ResolvableProfile> {

    private val cache = mutableMapOf<String, ResolvableProfile>()

    private const val TAG = "SkullOwner"

    override fun getComponentType(): DataComponentType<ResolvableProfile> = DataComponents.PROFILE
    override fun getData(compoundTag: CompoundTag): ResolvableProfile? {
        val skullOwner = compoundTag.getAndRemoveCompound(TAG) ?: return null

        val properties = skullOwner.getCompound("Properties").getOrNull() ?: return null
        val textures = properties.getList("textures").getOrNull() ?: return null
        val texture = textures.first().asCompound().getOrNull()?.getStringOrNull("Value") ?: return null

        return cache.getOrPut(texture) {
            val profile = GameProfile(UUID.randomUUID(), "a")
            profile.properties.put("textures", Property("textures", texture))
            ResolvableProfile(profile)
        }
    }
}
