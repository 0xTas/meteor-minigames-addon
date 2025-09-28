package dev.minigames.addon.gui.widgets.solitaire.render;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

/**
 * @author Tas [0xTas] <root@0xTas.dev>
 **/
public class PolygonRenderer {
    private static final int TOP_LEFT = 2;
    private static final int TOP_RIGHT = 1;
    private static final int BOTTOM_LEFT = 3;
    private static final int BOTTOM_RIGHT = 0;

    // Approximates polygons by emitting quad spans in a scanline fashion
    public static void drawFilledPolygon(GuiRenderer renderer, double[] xs, double[] ys, Color color) {
        int l = xs.length;
        if (l < 3) return;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        for (int n = 0; n < l; n++) {
            minY = Math.min(minY, ys[n]);
            maxY = Math.max(maxY, ys[n]);
        }

        double y1 = Math.ceil(maxY);
        double y0 = Math.floor(minY);

        for (double y = y0; y <= y1; y++) {
            List<Double> inter = new ArrayList<>();
            for (int n = 0; n < l; n++) {
                int j = (n + 1) % l;
                double x1 = xs[n], y1d = ys[n];
                double x2 = xs[j], y2d = ys[j];
                if (Math.abs(y1d - y2d) < 1e-12) continue;

                double yMinE = Math.min(y1d, y2d);
                double yMaxE = Math.max(y1d, y2d);
                if (y >= Math.ceil(yMinE) && y < Math.ceil(yMaxE)) {
                    double t = (y - y1d) / (y2d - y1d);
                    double xi = x1 + t * (x2 - x1);
                    inter.add(xi);
                }
            }

            if (inter.isEmpty()) continue;

            Collections.sort(inter);
            for (int k = 0; k + 1 < inter.size(); k += 2) {
                emitSpan(renderer, inter.get(k), inter.get(k + 1), y, color);
            }
        }
    }

    public static void drawFilledCircleSpan(GuiRenderer renderer, double cx, double cy, double r, Color color) {
        double rr = r * r;
        double y1 = Math.ceil(cy + r);
        double y0 = Math.floor(cy - r);
        for (double y = y0; y <= y1; y++) {
            double dy = y - cy;
            double val = rr - dy * dy;

            if (val <= 0) continue;
            double dx = Math.sqrt(val);

            double left = cx - dx;
            double right = cx + dx;
            emitSpan(renderer, left, right, y, color);
        }
    }

    private static void emitSpan(GuiRenderer renderer, double xLeft, double xRight, double y, Color color) {
        int leftPx  = (int) Math.ceil(xLeft - 0.5);
        int rightPx = (int) Math.floor(xRight + 0.5);

        if (rightPx < leftPx) return;
        double w = rightPx - leftPx + 1;
        renderer.quad(leftPx, y, w, 1.0, color);
    }

    public static void drawRoundedRect(
        GuiRenderer renderer, double rx, double ry,
        double rw, double rh, double cornerRadius, Color color
    ) {
        double radius = Math.min(rw, rh) / cornerRadius;
        renderer.quad(rx + radius, ry, rw - radius * 2, rh, color);
        renderer.quad(rx, ry + radius, radius, rh - radius * 2, color);
        renderer.quad(rx + rw - radius, ry + radius, radius, rh - radius * 2, color);

        // corners (quarter-circles via circle spans clipped to quarters)
        drawQuarterCircle(renderer, rx + radius, ry + radius, radius, TOP_LEFT, color);
        drawQuarterCircle(renderer, rx + rw - radius - 1, ry + radius, radius, TOP_RIGHT, color);
        drawQuarterCircle(renderer, rx + radius, ry + rh - radius - 1, radius, BOTTOM_LEFT, color);
        drawQuarterCircle(renderer, rx + rw - radius - 1, ry + rh - radius - 1, radius, BOTTOM_RIGHT, color);
    }

    public static void drawRoundedRectRotated(
        GuiRenderer renderer,
        double cx, double cy, double rw, double rh, double angleRad, Color color
    ) {
        int samples = Math.max(24, (int)Math.round(Math.max(rw, rh) / 4.0));

        double step = 1.0 / samples;
        for (int n = 0; n <= samples; n++) {
            double t = -0.5 + n * step;
            double localX = t * rw;
            double localY = 0;

            // rotate point by angleRad
            double rx = localX * Math.cos(angleRad) - localY * Math.sin(angleRad);
            double ry = localX * Math.sin(angleRad) + localY * Math.cos(angleRad);

            double px = cx + rx;
            double py = cy + ry;
            double sq = Math.max(2, Math.min(rw, rh) * 0.06);
            renderer.quad(px - sq / 2.0, py - sq / 2.0, sq, sq, color);
        }
    }

    private static void drawQuarterCircle(
        GuiRenderer renderer,
        double cx, double cy, double r, int quadrant, Color color
    ) {
        double rr = r * r;
        for (double dy = -r; dy <= r; dy++) {
            double y = cy + dy;
            double dxD = Math.sqrt(Math.max(0, rr - dy * dy));

            double x1 = cx - dxD;
            double x2 = cx + dxD;
            if (quadrant == BOTTOM_RIGHT) {
                if (y < cy) continue;
                double sx = Math.max(x1, cx);
                if (sx <= x2) renderer.quad(sx, y, x2 - sx + 1, 1, color);
            } else if (quadrant == TOP_RIGHT) {
                if (y > cy) continue;
                double sx = Math.max(x1, cx);
                if (sx <= x2) renderer.quad(sx, y, x2 - sx + 1, 1, color);
            } else if (quadrant == TOP_LEFT) {
                if (y > cy) continue;
                double ex = Math.min(x2, cx);
                if (x1 <= ex) renderer.quad(x1, y, ex - x1 + 1, 1, color);
            } else {
                if (y < cy) continue;
                double ex = Math.min(x2, cx);
                if (x1 <= ex) renderer.quad(x1, y, ex - x1 + 1, 1, color);
            }
        }
    }
}
