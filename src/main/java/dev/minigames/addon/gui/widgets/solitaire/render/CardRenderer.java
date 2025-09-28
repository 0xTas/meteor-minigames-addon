package dev.minigames.addon.gui.widgets.solitaire.render;

import java.util.List;
import java.util.ArrayList;
import dev.minigames.addon.gui.widgets.solitaire.model.Card;
import dev.minigames.addon.gui.widgets.solitaire.model.Rank;
import dev.minigames.addon.gui.widgets.solitaire.model.Suit;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import dev.minigames.addon.gui.widgets.solitaire.model.ColorScheme;

/**
 * @author Tas [0xTas] <root@0xTas.dev>
 **/
public class CardRenderer {
    private static final int TOP_TEXT_Y_OFFSET = 5;
    private static final int TOP_TEXT_X_OFFSET = 12;
    private static final int BOTTOM_TEXT_X_OFFSET = 24;
    private static final int BOTTOM_TEXT_Y_OFFSET = 44;
    private static final double CARD_BORDER_WIDTH = 1.0;
    public static final double CARD_CORNER_RESOLUTION = 16.0;

    public static void drawCard(GuiRenderer renderer, int x, int y,  int width, int height, boolean cullSuit, Card c, ColorScheme colors) {
        if (!c.faceUp) {
            drawCardFaceDown(renderer, x, y, width, height, colors);
            return;
        }

        drawCardBorder(
            renderer, x, y, width, height,
            CARD_CORNER_RESOLUTION, CARD_BORDER_WIDTH, colors.cardBorder
        );
        PolygonRenderer.drawRoundedRect(
            renderer, x, y, width, height,
            CARD_CORNER_RESOLUTION, colors.cardFaceColor
        );

        String rankText = c.rank.asString();
        Color suitColor = c.suit.isRed() ? colors.suitRed : colors.suitBlack;

        // rank + suit top-left
        renderer.text(
            rankText,
            x + TOP_TEXT_X_OFFSET - 1,
            y + TOP_TEXT_Y_OFFSET - 1,
            colors.textShadow, false
        );
        renderer.text(
            rankText,
            x + TOP_TEXT_X_OFFSET + 1,
            y + TOP_TEXT_Y_OFFSET - 1,
            colors.textShadow, false
        );
        renderer.text(rankText, x + TOP_TEXT_X_OFFSET, y + TOP_TEXT_Y_OFFSET, colors.rankColor, false);

        int smallSuitCenterX = x + 18;
        int smallSuitCenterY = y + 32;
        double smallSuitSize = Math.max(12, width * 0.14);

        drawSuit(
            renderer, smallSuitCenterX, smallSuitCenterY,
            smallSuitSize, c.suit, suitColor, colors.textShadow
        );

        if (!cullSuit) {
            // mirrored bottom-right
            renderer.text(
                rankText,
                x + width - BOTTOM_TEXT_X_OFFSET - 1,
                y + height - BOTTOM_TEXT_Y_OFFSET - 1,
                colors.textShadow, false
            );
            renderer.text(
                rankText,
                x + width - BOTTOM_TEXT_X_OFFSET + 1,
                y + height - BOTTOM_TEXT_Y_OFFSET - 1,
                colors.textShadow, false
            );
            renderer.text(
                rankText,
                x + width - BOTTOM_TEXT_X_OFFSET,
                y + height - BOTTOM_TEXT_Y_OFFSET,
                colors.rankColor, false
            );

            int brCenterX = x + width - 20;
            int brCenterY = y + height - 18;
            double brSize = Math.max(12, width * 0.14);

            drawSuit(
                renderer, brCenterX, brCenterY,
                brSize, c.suit, suitColor, colors.textShadow
            );
        }

        for (Vec pos : pipPositionsForRank(c.rank)) {
            int innerPadX = 12;
            int innerPadY = 24;
            int px = x + (int) (pos.x * (width - innerPadX * 2)) + innerPadX;
            int py = y + (int) (pos.y * (height - innerPadY * 2)) + innerPadY;

            double pipSize = Math.max(14, width * 0.16);

            drawSuit(
                renderer, px, py, pipSize,
                c.suit, suitColor, colors.textShadow
            );
        }
    }

    private static void drawCardFaceDown(GuiRenderer renderer, int x, int y, int width, int height, ColorScheme colors) {
        drawCardBorder(
            renderer, x, y, width, height,
            CARD_CORNER_RESOLUTION, CARD_BORDER_WIDTH, colors.cardBorder
        );
        PolygonRenderer.drawRoundedRect(
            renderer, x, y, width, height, CARD_CORNER_RESOLUTION, colors.cardBackColor
        );

        // rectangle inlay
        int cx = x + width / 2;
        int cy = y + height / 2;
        int minDimensions = Math.min(width, height);
        double vignetteInset = Math.max(6, minDimensions * 0.06);

        PolygonRenderer.drawRoundedRect(
            renderer, x + vignetteInset, y + vignetteInset,
            width - vignetteInset * 2, height - vignetteInset * 2, CARD_CORNER_RESOLUTION, colors.cardBorder
        );

        // circle inlays
        int petals = 6;
        int petalOuterR = Math.max(6, (int) Math.round(minDimensions * 0.14));
        int petalRingR = Math.max(14, (int) Math.round(minDimensions * 0.24));

        for (int n = 0; n < petals; n++) {
            double a = 2.0 * Math.PI * n / petals;
            double px = cx + Math.round(Math.cos(a) * petalRingR);
            double py = cy + Math.round(Math.sin(a) * petalRingR * 0.8);
            PolygonRenderer.drawFilledCircleSpan(renderer, px, py, petalOuterR * 0.9, colors.cardBackColor);
        }

        // banner & ribbon
        PolygonRenderer.drawRoundedRect(
            renderer, cx - minDimensions*0.28,  cy - minDimensions*0.06,
            minDimensions*0.56, Math.max(8, minDimensions*0.12), 6.0, colors.cardBorder
        );
        PolygonRenderer.drawRoundedRectRotated(
            renderer, cx, cy, minDimensions*0.56,
            Math.max(8, minDimensions*0.12), Math.toRadians(45), colors.cardBorder
        );

        // center star
        double outer = Math.max(18, minDimensions * 0.20);
        double inner = Math.max(8, outer * 0.45);

        double[][] star = starPolygon(cx, cy, outer, inner, 8);
        PolygonRenderer.drawFilledPolygon(renderer, star[0], star[1], colors.cardFaceColor);

        // smaller inner star
        double[][] innerStar = starPolygon(cx, cy, outer*0.69, inner*0.49, 8);
        PolygonRenderer.drawFilledPolygon(renderer, innerStar[0], innerStar[1], colors.cardBorder);
    }

    public static void drawCardBorder(
        GuiRenderer renderer,
        double rx, double ry, double rw, double rh,
        double cornerResolution, double borderWidth, Color color
    ) {
        if (cornerResolution == 0.0) {
            drawCardBorder(renderer, rx, ry, rw, rh, color);
            return;
        }

        PolygonRenderer.drawRoundedRect(
            renderer,
            rx - borderWidth, ry, rw, rh,
            CardRenderer.CARD_CORNER_RESOLUTION, color
        );
        PolygonRenderer.drawRoundedRect(
            renderer,
            rx + borderWidth, ry, rw, rh,
            CardRenderer.CARD_CORNER_RESOLUTION, color
        );
        PolygonRenderer.drawRoundedRect(
            renderer,
            rx, ry - borderWidth, rw, rh,
            CardRenderer.CARD_CORNER_RESOLUTION, color
        );
        PolygonRenderer.drawRoundedRect(
            renderer,
            rx, ry + borderWidth, rw, rh,
            CardRenderer.CARD_CORNER_RESOLUTION, color
        );
    }

    private static void drawCardBorder(GuiRenderer renderer, double x, double y, double width, double height, Color color) {
        renderer.quad(x, y, width, 1, color);
        renderer.quad(x, y, 1, height, color);
        renderer.quad(x + width - 1, y, 1, height, color);
        renderer.quad(x, y + height - 1, width, 1, color);
    }

    private static void drawSuit(GuiRenderer renderer, int cx, int cy, double sizePx, Suit suit, Color color, Color shadowColor) {
        drawSuitPolygons(renderer, cx + 1, cy + 1, sizePx, suit, shadowColor);
        drawSuitPolygons(renderer, cx, cy, sizePx, suit, color);
    }

    private static void drawSuitPolygons(GuiRenderer renderer, int cx, int cy, double sizePx, Suit suit, Color color) {
        int segments = Math.max(64, (int) Math.round(sizePx / 4.0));
        switch (suit) {
            case HEARTS -> {
                double[][] p = heartPolygon(cx, cy, sizePx, segments);
                PolygonRenderer.drawFilledPolygon(renderer, p[0], p[1], color);
            }
            case DIAMONDS -> {
                double[][] p = diamondPolygon(cx, cy, sizePx);
                PolygonRenderer.drawFilledPolygon(renderer, p[0], p[1], color);
            }
            case CLUBS -> {
                for (double[][] poly : clubPolygons(cx, cy, sizePx, segments)) {
                    PolygonRenderer.drawFilledPolygon(renderer, poly[0], poly[1], color);
                }

                double stemW = Math.max(1, sizePx * 0.18);
                double stemH = Math.max(1, sizePx * 0.29);
                renderer.quad(cx - stemW / 2.0, cy + sizePx * 0.28, stemW, stemH, color);
            }
            case SPADES -> {
                for (double[][] poly : spadePolygons(cx, cy, sizePx, segments)) {
                    PolygonRenderer.drawFilledPolygon(renderer, poly[0], poly[1], color);
                }

                double stemW = Math.max(1, sizePx * 0.16);
                double stemH = Math.max(1, sizePx * 0.35);
                renderer.quad(cx - stemW / 2.0, cy + sizePx * 0.32, stemW, stemH, color);
            }
        }
    }

    private static double[][] circlePolygon(double cx, double cy, double r, int segments) {
        double[] xs = new double[segments];
        double[] ys = new double[segments];
        for (int n = 0; n < segments; n++) {
            double a = 2.0 * Math.PI * n / segments;

            xs[n] = cx + Math.cos(a) * r;
            ys[n] = cy + Math.sin(a) * r;
        }

        return new double[][] { xs, ys };
    }

    // Heart: parametric heart curve sampled and normalized to desired size.
    private static double[][] heartPolygon(int cx, int cy, double size, int segments) {
        // sample parametric heart curve: x = 16 sin^3 t ; y = 13 cos t - 5 cos 2t - 2 cos 3t - cos 4t
        double[] xs = new double[segments];
        double[] ys = new double[segments];
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        for (int n = 0; n < segments; n++) {
            double t = 2.0 * Math.PI * n / segments;
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);

            xs[n] = x;
            ys[n] = y;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        double w = maxX - minX;
        double h = maxY - minY;
        double scale = size / Math.max(w, h);

        double[] ix = new double[segments];
        double[] iy = new double[segments];
        for (int n = 0; n < segments; n++) {
            ix[n] = cx + (xs[n] - (minX + w / 2.0)) * scale;
            iy[n] = cy - (ys[n] - (minY + h / 2.0)) * scale; // invert y so curve faces down
        }

        return new double[][] { ix, iy };
    }

    // Diamond: simple 4-point rhombus oriented vertically
    private static double[][] diamondPolygon(int cx, int cy, double size) {
        double halfW = size * 0.45;
        double halfH = size * 0.50;
        double[] xs = new double[] { cx, cx + halfW, cx, cx - halfW };
        double[] ys = new double[] { cy - halfH, cy, cy + halfH, cy };

        return new double[][] { xs, ys };
    }

    // Club: three circle-polys plus a stem, joined by drawing them in sequence.
    private static List<double[][]> clubPolygons(int cx, int cy, double size, int segments) {
        double r = size * 0.242;
        double offsetX = size * 0.36;
        double offsetY = size * 0.08;

        List<double[][]> out = new ArrayList<>();
        out.add(circlePolygon(cx - offsetX, cy - offsetY, r, segments)); // left
        out.add(circlePolygon(cx + offsetX, cy - offsetY, r, segments)); // right
        out.add(circlePolygon(cx, cy + size * 0.18, r, segments)); // bottom

        // center small circle to visually merge lobes
        out.add(circlePolygon(cx, cy, r * 0.77, segments));

        return out;
    }

    // Spade: an upward-pointing triangle shape overlaid with two lower circles + stem.
    private static List<double[][]> spadePolygons(int cx, int cy, double size, int segments) {
        double halfW = size * 0.55;
        double topY = cy - size * 0.45;
        double bottomY = cy + size * 0.15;

        // triangle tip vertices
        double[] ys = new double[] { topY, bottomY, bottomY };
        double[] xs = new double[] { cx, cx + halfW, cx - halfW };

        List<double[][]> out = new ArrayList<>();
        out.add(new double[][] { xs, ys });

        // two lobes
        double lobeR = size * 0.277;
        double lobeCenterY = cy + 4;
        double lobeOffsetX = size * 0.268;
        out.add(circlePolygon(cx - lobeOffsetX, lobeCenterY, lobeR, segments));
        out.add(circlePolygon(cx + lobeOffsetX, lobeCenterY, lobeR, segments));

        // small center circle to blend
        out.add(circlePolygon(cx, cy + size * 0.10, lobeR * 0.969, segments));

        return out;
    }

    @SuppressWarnings("SameParameterValue")
    private static double[][] starPolygon(double cx, double cy, double outerR, double innerR, int spikes) {
        if (spikes < 2) spikes = 2;

        int vertices = spikes * 2;
        double[] xs = new double[vertices];
        double[] ys = new double[vertices];

        // angle step for outer vertices
        double twoPi = Math.PI * 2.0;
        for (int k = 0; k < spikes; k++) {
            // outer vertex angle (pointing up at k=0)
            double baseAngle = -Math.PI / 2.0 + twoPi * k / spikes;
            int outerIndex = k * 2;
            int innerIndex = k * 2 + 1;

            // outer vertex
            xs[outerIndex] = cx + Math.cos(baseAngle) * outerR;
            ys[outerIndex] = cy + Math.sin(baseAngle) * outerR;

            // inner vertex is halfway to the next outer angle
            double innerAngle = baseAngle + Math.PI / spikes;
            xs[innerIndex] = cx + Math.cos(innerAngle) * innerR;
            ys[innerIndex] = cy + Math.sin(innerAngle) * innerR;
        }

        return new double[][] { xs, ys };
    }

    private static List<Vec> pipPositionsForRank(Rank r) {
        return switch (r) {
            case ACE -> List.of(new Vec(0.5, 0.35));
            case TWO -> List.of(new Vec(0.5, 0.22), new Vec(0.5, 0.78));
            case THREE -> List.of(new Vec(0.5, 0.16), new Vec(0.5, 0.5), new Vec(0.5, 0.84));
            case FOUR -> List.of(new Vec(0.28, 0.22), new Vec(0.72, 0.22), new Vec(0.28, 0.78), new Vec(0.72, 0.78));
            case FIVE -> List.of(new Vec(0.28, 0.22), new Vec(0.72, 0.22), new Vec(0.5, 0.5), new Vec(0.28, 0.78), new Vec(0.72, 0.78));
            case SIX -> List.of(new Vec(0.28, 0.16), new Vec(0.72, 0.16), new Vec(0.28, 0.5), new Vec(0.72, 0.5), new Vec(0.28, 0.84), new Vec(0.72, 0.84));
            case SEVEN -> {
                List<Vec> v = new ArrayList<>(pipPositionsForRank(Rank.SIX));
                v.addFirst(new Vec(0.5, 0.08));
                yield v;
            }
            case EIGHT -> {
                List<Vec> v = new ArrayList<>(pipPositionsForRank(Rank.SEVEN));
                v.add(new Vec(0.5, 0.92));
                yield v;
            }
            case NINE -> {
                List<Vec> v = new ArrayList<>(pipPositionsForRank(Rank.EIGHT));
                v.add(new Vec(0.5, 0.5));
                yield v;
            }
            case TEN -> List.of(
                new Vec(0.28, 0.12), new Vec(0.72, 0.12),
                new Vec(0.28, 0.28), new Vec(0.72, 0.28),
                new Vec(0.28, 0.5),  new Vec(0.72, 0.5),
                new Vec(0.28, 0.72), new Vec(0.72, 0.72),
                new Vec(0.28, 0.88), new Vec(0.72, 0.88)
            );
            case JACK, QUEEN, KING -> List.of(new Vec(0.5, 0.55));
        };
    }

    private record Vec(double x, double y) {}
}
