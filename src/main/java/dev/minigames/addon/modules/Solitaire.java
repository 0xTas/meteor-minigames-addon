package dev.minigames.addon.modules;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.StandardOpenOption;
import org.jetbrains.annotations.Nullable;
import dev.minigames.addon.MinigamesAddon;
import dev.minigames.addon.util.MinigamesUtil;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import dev.minigames.addon.gui.screens.SolitaireScreen;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import dev.minigames.addon.gui.widgets.solitaire.model.DrawMode;
import dev.minigames.addon.gui.widgets.solitaire.model.SaveState;
import dev.minigames.addon.gui.widgets.solitaire.model.ColorSchemes;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;

/**
 * @author Tas [0xTas] <root@0xTas.dev>
 *     See also: SolitaireScreen.java && dev/minigames/addon/gui/widgets/solitaire/*
 **/
public class Solitaire extends Module {
    public Solitaire() {
        super(MinigamesAddon.CATEGORY, "Solitaire", "What more could you ask for?");
        runInMainMenu = true;
    }

    private static final String GAME_FOLDER = "solitaire";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public final Setting<DrawMode> drawMode = settings.getDefaultGroup().add(
        new EnumSetting.Builder<DrawMode>()
            .name("draw-mode")
            .defaultValue(DrawMode.Draw_One)
            .build()
    );
    public final Setting<Boolean> shouldSave = settings.getDefaultGroup().add(
        new BoolSetting.Builder()
            .name("save-games")
            .description("Saves your game state when closing the Minesweeper screen.")
            .defaultValue(true)
            .build()
    );
    public final Setting<Boolean> sounds = settings.getDefaultGroup().add(
        new BoolSetting.Builder()
            .name("game-sounds")
            .description("Plays game sounds.")
            .defaultValue(true)
            .build()
    );
    public final Setting<Double> soundVolume = settings.getDefaultGroup().add(
        new DoubleSetting.Builder()
            .name("sounds-volume")
            .min(0.1).max(4.0)
            .defaultValue(0.5)
            .visible(sounds::get)
            .build()
    );
    public final Setting<ColorSchemes> colorScheme = settings.getDefaultGroup().add(
        new EnumSetting.Builder<ColorSchemes>()
            .name("color-scheme")
            .defaultValue(ColorSchemes.Themed)
            .build()
    );

    public @Nullable SaveState saveData = null;

    public void saveGame(SaveState data) {
        saveData = data;
        Path saveFolder = MinigamesUtil.GAME_FOLDER.toPath().resolve(GAME_FOLDER);

        //noinspection ResultOfMethodCallIgnored
        saveFolder.toFile().mkdirs();
        Path save = saveFolder.resolve("save.json");
        if (!Files.exists(save)) {
            try {
                Files.createFile(save);
            } catch (Exception err) {
                error("Failed to create save file§c..!", this.name);
                MinigamesAddon.LOG.error("Failed to create Solitaire save file..! Why: {}", err.toString());
            }
        }
        try (Writer writer = Files.newBufferedWriter(save, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(data, writer);
        } catch (Exception err) {
            MinigamesAddon.LOG.error("Solitaire: {}", err.toString());
        }
    }

    public void clearSave() {
        saveGame(null);
    }

    @Override
    public void onActivate() {
        Path saveFolder = MinigamesUtil.GAME_FOLDER.toPath().resolve(GAME_FOLDER);

        //noinspection ResultOfMethodCallIgnored
        saveFolder.toFile().mkdirs();
        Path save = saveFolder.resolve("save.json");
        if (!Files.exists(save)) {
            try {
                Files.createFile(save);
            } catch (Exception err) {
                error("Failed to create save file§c..!", this.name);
                MinigamesAddon.LOG.error("Failed to create a Solitaire save file..! Why: {}", err.toString());
            }
        }

        SaveState data = null;
        try (Reader reader = Files.newBufferedReader(save)) {
            data = GSON.fromJson(reader, SaveState.class);
        } catch (Exception err) {
            MinigamesAddon.LOG.error("[Solitaire]: {}", err.toString());
        }

        if (data != null) saveData = data;

        try {
            mc.setScreen(new SolitaireScreen(this, GuiThemes.get(), "Solitaire"));
        } catch (Exception err) {
            MinigamesAddon.LOG.error("Failed to open the Solitaire screen: {}", err.toString());
            toggle();
            sendToggledMsg();
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.currentScreen instanceof SolitaireScreen) {
            try {
                mc.setScreen(null);
            } catch (Exception err) {
                MinigamesAddon.LOG.error("Failed to close the Solitaire screen: {}", err.toString());
                toggle();
                sendToggledMsg();
            }
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList list = theme.horizontalList();
        WButton clearSave = list.add(theme.button("Clear Save")).widget();

        clearSave.action = this::clearSave;

        return list;
    }
}
