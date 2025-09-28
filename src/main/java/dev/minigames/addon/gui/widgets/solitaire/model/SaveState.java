package dev.minigames.addon.gui.widgets.solitaire.model;

import java.util.List;

public record SaveState(
    DrawMode mode, List<Card> stock, List<Card> waste,
    List<List<Card>> foundations, List<List<Card>> tableau, long accumulatedMillis
) {}
