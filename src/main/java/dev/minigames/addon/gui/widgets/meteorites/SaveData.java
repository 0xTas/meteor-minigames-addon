package dev.minigames.addon.gui.widgets.meteorites;

import dev.minigames.addon.gui.widgets.meteorites.entity.Ship;
import dev.minigames.addon.gui.widgets.meteorites.entity.Bullet;
import dev.minigames.addon.gui.widgets.meteorites.entity.Meteorite;

public record SaveData(
    int version, long seed, FieldSize fieldSize, Ship player,
    Meteorite[] meteorites, Bullet[] bullets, boolean cheating,
    int waves, int meteoriteCount, int bulletCount, double accumulator, long pausedAt
) {}

