package dev.minigames.addon;

import com.mojang.logging.LogUtils;
import dev.minigames.addon.modules.Meteorites;
import dev.minigames.addon.modules.Minesweeper;
import dev.minigames.addon.util.AddonUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.slf4j.Logger;

public class MinigamesAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Minigames", AddonUtil.getCategoryIcon());

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Minigames Addon..!");

        Modules.get().add(new Meteorites());
        Modules.get().add(new Minesweeper());

        LOG.info("Meteor Minigames Addon initialized successfully!");
    }

    @Override
    public String getWebsite() { return "https://github.com/0xTas/meteor-minigames-addon"; }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "dev.minigames.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("0xTas", "meteor-minigames-addon");
    }

    @Override
    public String getCommit() {
        CustomValue commit = FabricLoader.getInstance()
            .getModContainer("minigames-addon")
            .orElseThrow()
            .getMetadata()
            .getCustomValue(MeteorClient.MOD_ID + ":commit");

        return commit == null ? null : commit.getAsString();
    }
}
