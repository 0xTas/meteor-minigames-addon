package dev.minigames.addon.gui.widgets.solitaire.model;

public enum DrawMode {
    Draw_One, Draw_Three;

    public int count() {
        return switch (this) {
            case Draw_One -> 1;
            case Draw_Three -> 3;
        };
    }
}
