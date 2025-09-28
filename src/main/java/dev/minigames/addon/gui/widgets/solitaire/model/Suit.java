package dev.minigames.addon.gui.widgets.solitaire.model;

import net.minecraft.util.StringIdentifiable;

public enum Suit implements StringIdentifiable {
    HEARTS, DIAMONDS, CLUBS, SPADES;

    public boolean isRed() {
        return this == Suit.HEARTS || this == Suit.DIAMONDS;
    }

    @Override
    public String asString() {
        return switch (this) {
            case CLUBS -> "♣";
            case SPADES -> "♠";
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
        };
    }
}
