package com.github.zly2006.reden;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.network.ChannelsKt;
import com.github.zly2006.reden.utils.ResourceLoader;
import com.github.zly2006.reden.utils.UtilsKt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Reden implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "reden-undo";
    public static final String MOD_NAME = "Reden";
    public static final String CONFIG_FILE = "reden.json";
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static final Version MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String version() {
        return "reden";
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(RedenCarpetSettings.Options.class);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return ResourceLoader.loadLang(lang);
    }


    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(UtilsKt::setServer);
        ChannelsKt.register();
        CarpetServer.manageExtension(this);
        Sounds.init();
    }

    @Contract("_ -> new")
    public static @NotNull Identifier identifier(@NotNull String id) {
        return new Identifier(MOD_ID, id);
    }
}
