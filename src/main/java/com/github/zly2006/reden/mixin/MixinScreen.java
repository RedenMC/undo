package com.github.zly2006.reden.mixin;

import com.github.zly2006.reden.RedenClient;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class MixinScreen {
    @Shadow @Nullable public MinecraftClient client;

    @Inject(
            method = "handleTextClick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/option/GameOptions;chatLinks:Z"
            ),
            cancellable = true
    )
    private void redenClickEvent(Style style, CallbackInfoReturnable<Boolean> cir) {
        String value = style.getClickEvent().getValue();
        if (value.startsWith("reden:")) {
            String command = value.substring(6);
            if (command.startsWith("malilib:")) {
                String content = command.substring(8);
                String key = content.substring(0, content.indexOf("="));
                String val = content.substring(content.indexOf("=") + 1);
                MalilibSettingsKt.getAllOptions().stream()
                        .filter(it -> it.getName().equals(key))
                        .forEach(it -> it.setValueFromJsonElement(new JsonPrimitive(val)));
                RedenClient.saveMalilibOptions();
            }
            else if (command.equals("stopClient")) {
                client.stop();
            }
            else if (command.startsWith("playSound:")) {

            }
            cir.setReturnValue(true);
        }
    }
}
