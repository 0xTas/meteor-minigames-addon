package dev.minigames.addon.gui.widgets.solitaire.model;

import net.minecraft.util.StringIdentifiable;

public enum Rank implements StringIdentifiable {
    ACE, TWO, THREE, FOUR,
    FIVE, SIX, SEVEN, EIGHT,
    NINE, TEN, JACK, QUEEN, KING;

    @Override
    public String asString() {
        return switch (this) {
            case ACE -> "A";
            case TWO -> "2";
            case THREE -> "3";
            case FOUR -> "4";
            case FIVE -> "5";
            case SIX -> "6";
            case SEVEN -> "7";
            case EIGHT -> "8";
            case NINE -> "9";
            case TEN -> "10";
            case JACK -> "J";
            case QUEEN -> "Q";
            case KING -> "K";
        };
    }
}
