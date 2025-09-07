package dev.minigames.addon.util;

import java.io.File;
import net.fabricmc.loader.api.FabricLoader;

public class MinigamesUtil {
    public static File GAME_FOLDER = FabricLoader.getInstance().getGameDir().resolve("meteor-client/minigames-addon").toFile();
}
