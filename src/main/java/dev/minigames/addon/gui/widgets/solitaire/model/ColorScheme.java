package dev.minigames.addon.gui.widgets.solitaire.model;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;

public class ColorScheme {
    public Color backgroundColor;
    public Color cardFaceColor;
    public Color cardBackColor;
    public Color cardBorder;
    public Color emptyPileColor;
    public Color textShadow;
    public Color rankColor;
    public Color suitRed;
    public Color suitBlack;
    public Color timerColor;
    public Color statusBarColor;
    public Color statusTextColor;
    public Color buttonColor;
    public Color buttonHoveredColor;

    public ColorScheme(ColorSchemes scheme, GuiTheme theme) {
        boolean assignDefaults = false;
        switch (scheme) {
            case Themed -> {
                if (theme instanceof MeteorGuiTheme gt) {
                    this.backgroundColor = gt.backgroundColor.get();
                    this.cardFaceColor = gt.accentColor.get();
                    this.cardBackColor = gt.moduleBackground.get();
                    this.cardBorder = gt.outlineColor.get();

                    this.rankColor = gt.titleTextColor.get();
                    this.timerColor = gt.textColor.get();
                    this.statusTextColor = gt.textSecondaryColor.get();
                    this.statusBarColor = gt.accentColor.get();
                    this.buttonColor = gt.scrollbarColor.get();
                    this.textShadow = gt.backgroundColor.get(false, true, true);
                    this.emptyPileColor = gt.backgroundColor.get(false, true, true);
                    this.buttonHoveredColor = gt.scrollbarColor.get(false, true, true);
                    this.suitRed = new Color(200, 40, 40);
                    this.suitBlack = new Color(32, 32, 32);

                    // ensure cards are max opacity
                    this.cardFaceColor = new Color(
                        this.cardFaceColor.r,
                        this.cardFaceColor.g,
                        this.cardFaceColor.b
                    );
                    this.cardBackColor = new Color(
                        this.cardBackColor.r,
                        this.cardBackColor.g,
                        this.cardBackColor.b
                    );
                } else {
                    assignDefaults = true;
                }
            }
            default -> assignDefaults = true;
        }

        if (assignDefaults) {
            this.backgroundColor = new Color(18, 110, 51);
            this.cardFaceColor = new Color(248, 248, 248);
            this.cardBackColor = new Color(132, 96, 160);
            this.cardBorder = new Color(0, 0, 0);
            this.emptyPileColor = new Color(100, 180, 100);
            this.textShadow = new Color(0, 0, 0);
            this.rankColor = new Color(0, 0, 0);
            this.suitRed = new Color(200, 40, 40);
            this.suitBlack = new Color(32, 32, 32);
            this.timerColor = new Color(242, 242, 242);
            this.statusTextColor = new Color(242, 242, 13);
            this.statusBarColor = new Color(113, 113, 113);
            this.buttonColor = new Color(69, 69, 69);
            this.buttonHoveredColor = new Color(113, 113, 113);
        }
    }
}

