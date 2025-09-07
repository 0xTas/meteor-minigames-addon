package dev.minigames.addon.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class MinigamesUtil {
    public static File GAME_FOLDER = FabricLoader.getInstance().getGameDir().resolve("meteor-client/minigames-addon").toFile();
}
