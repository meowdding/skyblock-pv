package tech.thatgravyboat.skyblockpv.utils

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class SkyblockPvMixinPlugin : IMixinConfigPlugin {

    override fun shouldApplyMixin(targetClassName: String?, mixinClassName: String?): Boolean {
        if (targetClassName?.contains("skyblockpv.mixin.compat.") == true) {
            val path = targetClassName.split("skyblockpv.mixin.compat.")[1]
            val mod = path.split("\\.")[0]
            return FabricLoader.getInstance().isModLoaded(mod)
        }
        return true
    }

    override fun onLoad(mixinPackage: String?) {}

    override fun getRefMapperConfig(): String? = null

    override fun acceptTargets(myTargets: Set<String?>?, otherTargets: Set<String?>?) {}

    override fun getMixins(): List<String?>? = null

    override fun preApply(targetClassName: String?, targetClass: ClassNode?, mixinClassName: String?, mixinInfo: IMixinInfo?) {}

    override fun postApply(targetClassName: String?,targetClass: ClassNode?, mixinClassName: String?, mixinInfo: IMixinInfo?, ) {}
}
