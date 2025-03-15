package tech.thatgravyboat.skyblockpv.mixin;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@Mixin(Language.class)
public class LanguageMixin {

    @Shadow
    @Final
    private static Pattern UNSUPPORTED_FORMAT_PATTERN;

    @Inject(
        method = "loadFromJson",
        at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonObject;entrySet()Ljava/util/Set;")
    )
    private static void loadFromJsonInject(
        InputStream ignored,
        BiConsumer<String, String> output,
        CallbackInfo ci,
        @Local(ordinal = 0) JsonObject json
    ) {
        if (json.has("@metadata")) {
            var metadata = json.remove("@metadata").getAsJsonObject();
            var canUseArrays = metadata.has("allowArrays") && metadata.get("allowArrays").getAsBoolean();

            for (var entry : json.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                if (value.isJsonArray()) {
                    if (!canUseArrays) throw new IllegalArgumentException("Arrays are not allowed in this language file");
                    var array = value.getAsJsonArray();
                    String[] values = new String[array.size()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = skyblockpv$fixString(array.get(i).getAsString());
                    }
                    output.accept(key, String.join("\n", values));
                } else {
                    output.accept(key, skyblockpv$fixString(value.getAsString()));
                }
            }
        }
    }

    @Unique
    private static String skyblockpv$fixString(String str) {
        return UNSUPPORTED_FORMAT_PATTERN.matcher(str).replaceAll("%$1s");
    }
}
