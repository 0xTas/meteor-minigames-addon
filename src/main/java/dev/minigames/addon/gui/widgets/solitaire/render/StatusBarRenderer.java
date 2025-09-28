package dev.minigames.addon.gui.widgets.solitaire.render;

import dev.minigames.addon.gui.widgets.solitaire.WSolitaire;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import static dev.minigames.addon.gui.widgets.solitaire.WSolitaire.*;

/**
 * @author Tas [0xTas] <root@0xTas.dev>
 **/
public class StatusBarRenderer {
    public static void render(GuiRenderer renderer, int bx, int by, double width, WSolitaire widget) {
        renderer.quad(bx, by, width, STATUS_BAR_HEIGHT, widget.colors.statusBarColor);

        long elapsedSec = 0L;
        if (widget.gameStart > 0 && !widget.gameOver) {
            elapsedSec = (System.currentTimeMillis() - (widget.gameStart - widget.accumulated)) / 1000;
        } else if (widget.gameOver) elapsedSec = (widget.gameEnd - (widget.gameStart - widget.accumulated)) / 1000;

        double textHeight = TextRenderer.get().getHeight();
        String timerText = String.format("%02d:%02d", (elapsedSec / 60), (elapsedSec % 60));

        double scaledHeight = textHeight * 1.25;
        double timerTextWidth = TextRenderer.get().getWidth(timerText) * 1.25;
        double timerX = bx + (width / 2.0) - (timerTextWidth / 2.0);
        renderer.text(timerText, timerX - 1, by + STATUS_BAR_HEIGHT - scaledHeight - 2, widget.colors.textShadow, true);
        renderer.text(timerText, timerX + 1, by + STATUS_BAR_HEIGHT - scaledHeight - 2, widget.colors.textShadow, true);
        renderer.text(timerText, timerX, by + STATUS_BAR_HEIGHT - scaledHeight - 1, widget.colors.textShadow, true);
        renderer.text(timerText, timerX, by + STATUS_BAR_HEIGHT - scaledHeight - 2, widget.colors.timerColor, true);

        int resetButtonY = by + 3;
        int resetButtonX = (int) (bx + width - RESET_BUTTON_WIDTH - 6);

        renderer.quad(
            resetButtonX, resetButtonY,
            RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT,
            widget.resetButtonHovered ? widget.colors.buttonHoveredColor : widget.colors.buttonColor
        );

        String reset = "Reset";
        double resetTextWidth = TextRenderer.get().getWidth(reset);
        double resetTextX = resetButtonX + (RESET_BUTTON_WIDTH / 2.0) - (resetTextWidth / 2.0);
        renderer.text(
            reset, resetTextX - 1,
            resetButtonY + RESET_BUTTON_HEIGHT - textHeight - 3, widget.colors.textShadow, false
        );
        renderer.text(
            reset, resetTextX + 1,
            resetButtonY + RESET_BUTTON_HEIGHT - textHeight - 3, widget.colors.textShadow, false
        );
        renderer.text(
            reset, resetTextX,
            resetButtonY + RESET_BUTTON_HEIGHT - textHeight - 2, widget.colors.textShadow, false
        );
        renderer.text(
            reset, resetTextX,
            resetButtonY + RESET_BUTTON_HEIGHT - textHeight - 3, widget.colors.statusTextColor, false
        );

        if (widget.undoButtonVisible) {
            int undoButtonY = by + 3;
            int undoButtonX = (int) (bx + width - RESET_BUTTON_WIDTH - 12 - UNDO_BUTTON_WIDTH);

            renderer.quad(
                undoButtonX, undoButtonY,
                UNDO_BUTTON_WIDTH, UNDO_BUTTON_HEIGHT,
                widget.undoButtonHovered ? widget.colors.buttonHoveredColor : widget.colors.buttonColor
            );

            String undo = "Undo";
            double undoTextWidth = TextRenderer.get().getWidth(undo);
            double undoTextX = undoButtonX + (UNDO_BUTTON_WIDTH / 2.0) - (undoTextWidth / 2.0);
            renderer.text(
                undo, undoTextX - 1,
                undoButtonY + UNDO_BUTTON_HEIGHT - textHeight - 3, widget.colors.textShadow, false
            );
            renderer.text(
                undo, undoTextX + 1,
                undoButtonY + UNDO_BUTTON_HEIGHT - textHeight - 3, widget.colors.textShadow, false
            );
            renderer.text(
                undo, undoTextX,
                undoButtonY + UNDO_BUTTON_HEIGHT - textHeight - 2, widget.colors.textShadow, false
            );
            renderer.text(
                undo, undoTextX,
                undoButtonY + UNDO_BUTTON_HEIGHT - textHeight - 3, widget.colors.statusTextColor, false
            );
        }

        if (widget.gameOver) {
            String gameOverText;
            if (widget.gameWon) {
                gameOverText = "You win..!";
            } else {
                gameOverText = "You lost..?";
            }
            renderer.text(
                gameOverText, bx + LEFT_PAD_PX - 1,
                by + (STATUS_BAR_HEIGHT / 2.0) - (scaledHeight / 2.0) - 1,
                widget.colors.textShadow, true
            );
            renderer.text(
                gameOverText, bx + LEFT_PAD_PX + 1,
                by + (STATUS_BAR_HEIGHT / 2.0) - (scaledHeight / 2.0) - 1,
                widget.colors.textShadow, true
            );
            renderer.text(
                gameOverText, bx + LEFT_PAD_PX,
                by + (STATUS_BAR_HEIGHT / 2.0) - (scaledHeight / 2.0),
                widget.colors.textShadow, true
            );
            renderer.text(
                gameOverText, bx + LEFT_PAD_PX,
                by + (STATUS_BAR_HEIGHT / 2.0) - (scaledHeight / 2.0) - 1,
                widget.gameWon ? new Color(13, 255, 13) : new Color(255, 13, 13), true
            );
        }
    }
}
