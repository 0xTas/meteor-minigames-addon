package dev.minigames.addon.gui.widgets.solitaire.model;

public class Card {
    public Rank rank;
    public Suit suit;
    public boolean faceUp;

    public Card(Rank r, Suit s) {
        this.rank = r;
        this.suit = s;
    }

    @Override public String toString() {
        return rank.asString() + suit.asString();
    }
}
