package me.owdding.skyblockpv.utils.codecs

import net.minecraft.core.ClientAsset
import net.minecraft.resources.ResourceLocation

internal fun clientAssetConverter(): (ResourceLocation) -> ClientAsset = ClientAsset::ResourceTexture
