package dev.minigames.addon.gui.widgets.solitaire;

import java.util.*;
import static org.lwjgl.glfw.GLFW.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;
import dev.minigames.addon.modules.Solitaire;
import java.util.concurrent.ThreadLocalRandom;
import meteordevelopment.meteorclient.gui.GuiTheme;
import dev.minigames.addon.gui.widgets.solitaire.model.*;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import dev.minigames.addon.gui.widgets.solitaire.input.InputTracker;
import dev.minigames.addon.gui.widgets.solitaire.render.CardRenderer;
import dev.minigames.addon.gui.widgets.solitaire.render.PolygonRenderer;
import dev.minigames.addon.gui.widgets.solitaire.render.StatusBarRenderer;
import static dev.minigames.addon.gui.widgets.solitaire.input.InputTracker.isKeyDown;

/**
 * @author Tas [0xTas] <root@0xTas.dev>
 **/
@SuppressWarnings("UnnecessaryLocalVariable")
public class WSolitaire extends WWidget {
    public static final int TOP_PAD_PX = 6;
    public static final int LEFT_PAD_PX = 12;
    public static final int CARD_WIDTH = 169;
    public static final int CARD_HEIGHT = 272;
    public static final int PILE_SPACING = 12;
    public static final int TABLEAU_COUNT = 7;
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 1280;
    public static final int FOUNDATION_COUNT = 4;
    public static final int TAB_OVERLAP_FACEUP = 56;
    public static final int TAB_OVERLAP_FACEDOWN = 24;
    public static final int WASTE_DRAW_3_OVERLAP = 56;
    private static final long AUTO_MOVE_INTERVAL = 169L;

    public static final int STATUS_BAR_HEIGHT = 32;
    public static final int UNDO_BUTTON_WIDTH = 64;
    public static final int RESET_BUTTON_WIDTH = 64;
    public static final int UNDO_BUTTON_HEIGHT = STATUS_BAR_HEIGHT - 6;
    public static final int RESET_BUTTON_HEIGHT = STATUS_BAR_HEIGHT - 6;

    private List<Card> stock = new ArrayList<>();
    private List<Card> waste = new ArrayList<>();
    private List<List<Card>> tableau = new ArrayList<>();
    private List<List<Card>> foundations = new ArrayList<>();

    private double dragX, dragY;
    public boolean dragging = false;
    private int dragOriginIndex = -1;
    private @Nullable Move lastMove = null;
    private List<Card> dragOriginPile = null;
    private List<Card> draggedCards = new ArrayList<>();

    public long gameEnd;
    public long gameStart;
    public long accumulated;
    private double lastMouseX, lastMouseY;

    public boolean gameWon;
    public boolean gameOver;
    public boolean undoButtonVisible;
    public boolean undoButtonHovered;
    public boolean resetButtonHovered;

    private boolean autoMoving = false;
    private long nextAutoMoveTime = 0L;
    private final Deque<AutoMove> autoMoveQueue = new ArrayDeque<>();

    public final Solitaire module;
    public final DrawMode drawMode;
    public final ColorScheme colors;
    private final InputTracker inputTracker;
    private final Random rng = ThreadLocalRandom.current();

    public WSolitaire(Solitaire module, GuiTheme theme) {
        this.module = module;
        this.inputTracker = new InputTracker();
        this.colors = new ColorScheme(module.colorScheme.get(), theme);

        if (module.shouldSave.get() && module.saveData != null && module.drawMode.get() == module.saveData.mode()) {
            this.stock = module.saveData.stock();
            this.waste = module.saveData.waste();
            this.drawMode = module.saveData.mode();
            this.tableau = module.saveData.tableau();
            this.gameStart = System.currentTimeMillis();
            this.foundations = module.saveData.foundations();
            this.accumulated += module.saveData.accumulatedMillis();

            return;
        }

        this.drawMode = module.drawMode.get();

        for (int n = 0; n < TABLEAU_COUNT; n++)
            tableau.add(new ArrayList<>());

        for (int n = 0; n < FOUNDATION_COUNT; n++)
            foundations.add(new ArrayList<>());

        initEmpty();
    }

    @Override
    protected void onCalculateSize() {
        width = WINDOW_WIDTH;
        height = WINDOW_HEIGHT;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (autoMoving && !autoMoveQueue.isEmpty()) {
            long now = System.currentTimeMillis();
            if (now >= nextAutoMoveTime) {
                AutoMove move = autoMoveQueue.pollFirst();
                if (move != null) {
                    List<Card> origin = tableau.get(move.fromPileIdx);
                    if (!origin.isEmpty() && origin.getLast() == move.card) {
                        if (tryAutoMoveToFoundations(move.card, origin)) {
                            origin.removeLast();
                            playSound(
                                SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                                rng.nextFloat(0.77f, 1.420f),
                                module.soundVolume.get().floatValue()
                            );
                            nextAutoMoveTime = System.currentTimeMillis() + AUTO_MOVE_INTERVAL;
                        } else {
                            autoMoveQueue.clear();
                        }
                    }
                }
            }
        }

        if (autoMoving && autoMoveQueue.isEmpty()) {
            autoMoving = false;
        }

        pollInput();
        int bx = (int) x;
        int by = (int) y;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        undoButtonVisible = lastMove != null;

        // background
        renderer.quad(
            bx - 2, by - 2,
            width, height, colors.backgroundColor
        );
        StatusBarRenderer.render(renderer, bx, by, width, this);

        by += STATUS_BAR_HEIGHT;
        int stockY = by + TOP_PAD_PX;
        int stockX = bx + LEFT_PAD_PX;

        int wasteY = stockY;
        int wasteX = stockX + CARD_WIDTH + PILE_SPACING;
        int foundationsStartX = bx + LEFT_PAD_PX + 3 * (CARD_WIDTH + PILE_SPACING);

        if (stock.isEmpty()) {
            CardRenderer.drawCardBorder(
                renderer,
                stockX, stockY, CARD_WIDTH, CARD_HEIGHT,
                CardRenderer.CARD_CORNER_RESOLUTION, 1.0, colors.cardBorder
            );
            PolygonRenderer.drawRoundedRect(
                renderer,
                stockX, stockY, CARD_WIDTH, CARD_HEIGHT,
                CardRenderer.CARD_CORNER_RESOLUTION, colors.emptyPileColor
            );
        } else {
            Card top = stock.getLast();

            top.faceUp = false;
            CardRenderer.drawCard(renderer, stockX, stockY, CARD_WIDTH, CARD_HEIGHT, false, top, colors);
        }

        if (drawMode.equals(DrawMode.Draw_Three)) {
            int visibleStart;
            if (waste.size() > 3) visibleStart = waste.size() - 3;
            else visibleStart = 0;

            // first draw borders
            int offset = 0;
            for (int n = visibleStart; n < Math.max(waste.size(), 3); n++) {
                int cx = wasteX + (WASTE_DRAW_3_OVERLAP * offset);

                CardRenderer.drawCardBorder(
                    renderer, cx, wasteY,
                    CARD_WIDTH, CARD_HEIGHT,
                    CardRenderer.CARD_CORNER_RESOLUTION, 1.0, colors.cardBorder
                );
                PolygonRenderer.drawRoundedRect(
                    renderer,
                    cx, wasteY, CARD_WIDTH, CARD_HEIGHT,
                    CardRenderer.CARD_CORNER_RESOLUTION, colors.emptyPileColor
                );

                ++offset;
            }

            offset = 0;
            if (!waste.isEmpty()) for (int n = visibleStart; n < waste.size(); n++) {
                int cx = wasteX + (WASTE_DRAW_3_OVERLAP * offset);

                Card c = waste.get(n);
                if (n == waste.size() - 1) {
                    if (!dragging || !draggedCards.contains(c)) {
                        CardRenderer.drawCard(
                            renderer, cx, wasteY,
                            CARD_WIDTH, CARD_HEIGHT, false, c, colors
                        );
                    }
                } else {
                    boolean cullSuit = n < waste.size() - 2
                        || (n == waste.size() - 2
                        && (!dragging || !draggedCards.contains(waste.getLast())));

                    CardRenderer.drawCard(
                        renderer, cx, wasteY,
                        CARD_WIDTH, CARD_HEIGHT, cullSuit, c, colors
                    );
                }

                ++offset;
            }
        } else {
            CardRenderer.drawCardBorder(
                renderer,
                wasteX, wasteY, CARD_WIDTH, CARD_HEIGHT,
                CardRenderer.CARD_CORNER_RESOLUTION, 1.0, colors.cardBorder
            );
            PolygonRenderer.drawRoundedRect(
                renderer,
                wasteX, wasteY, CARD_WIDTH, CARD_HEIGHT,
                CardRenderer.CARD_CORNER_RESOLUTION, colors.emptyPileColor
            );

            if (!waste.isEmpty()) {
                Card top = waste.getLast();
                if (!dragging || !draggedCards.contains(top)) {
                    CardRenderer.drawCard(
                        renderer, wasteX, wasteY,
                        CARD_WIDTH, CARD_HEIGHT, false, top, colors
                    );
                }
            }
        }

        renderer.scissorStart(bx, by, width, height);

        for (int n = 0; n < FOUNDATION_COUNT; n++) {
            int fy = TOP_PAD_PX + by;
            int fx = foundationsStartX + n * (CARD_WIDTH + PILE_SPACING);

            CardRenderer.drawCardBorder(
                renderer,
                fx, fy, CARD_WIDTH, CARD_HEIGHT,
                CardRenderer.CARD_CORNER_RESOLUTION, 1.0, colors.cardBorder
            );
            PolygonRenderer.drawRoundedRect(
                renderer,
                fx, fy, CARD_WIDTH, CARD_HEIGHT,
                CardRenderer.CARD_CORNER_RESOLUTION, colors.emptyPileColor
            );

            List<Card> f = foundations.get(n);
            if (!f.isEmpty()) {
                Card fc = f.getLast();
                if (!dragging || !draggedCards.contains(fc)) {
                    CardRenderer.drawCard(renderer, fx, fy, CARD_WIDTH, CARD_HEIGHT, false,fc, colors);
                }
            }
        }

        int tableauStartY = by + TOP_PAD_PX + CARD_HEIGHT + PILE_SPACING;

        for (int pi = 0; pi < TABLEAU_COUNT; pi++) {
            int py = tableauStartY;
            int px = bx + LEFT_PAD_PX + pi * (CARD_WIDTH + PILE_SPACING);

            CardRenderer.drawCardBorder(
                renderer, px, py,
                CARD_WIDTH, CARD_HEIGHT,
                CardRenderer.CARD_CORNER_RESOLUTION, 1.0, colors.cardBorder
            );
            PolygonRenderer.drawRoundedRect(
                renderer,
                px, py, CARD_WIDTH, CARD_HEIGHT,
                CardRenderer.CARD_CORNER_RESOLUTION, colors.emptyPileColor
            );

            List<Card> pile = tableau.get(pi);

            if (!pile.isEmpty()) {
                int n = 0;
                int curY = py;
                for (Card c : pile) {
                    if (!dragging || !draggedCards.contains(c)) {
                        boolean cullSuit = n != pile.size() - 1
                            && (!dragging || !draggedCards.contains(pile.get(Math.min(pile.size() - 1, n + 1))));
                        CardRenderer.drawCard(renderer, px, curY, CARD_WIDTH, CARD_HEIGHT, cullSuit,c, colors);

                        curY += c.faceUp ? TAB_OVERLAP_FACEUP : TAB_OVERLAP_FACEDOWN;
                    }
                    ++n;
                }
            }
        }

        renderer.scissorEnd();

        // dragged cards last so they draw on top
        if (dragging && !draggedCards.isEmpty()) {
            double baseX = dragX;
            double baseY = dragY;
            for (int n = 0; n < draggedCards.size(); n++) {
                int dx = (int) (baseX);
                int dy = (int) (baseY + n * TAB_OVERLAP_FACEUP);

                CardRenderer.drawCard(
                    renderer, dx + bx, dy + by, CARD_WIDTH, CARD_HEIGHT,
                    (n < draggedCards.size() - 1), draggedCards.get(n), colors
                );
            }
        }
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        double localX = mouseX - x;
        double localY = mouseY - y;

        if (localY >= 0 && localY < STATUS_BAR_HEIGHT) {
            int buttonY = 3;
            int resetButtonX = (int) width - RESET_BUTTON_WIDTH - 6;
            int undoButtonX = (int) width - RESET_BUTTON_WIDTH - 12 - UNDO_BUTTON_WIDTH;

            if (localX >= resetButtonX && localX <= resetButtonX + RESET_BUTTON_WIDTH
                && localY >= buttonY && localY <= buttonY + RESET_BUTTON_HEIGHT) {
                initEmpty();
                return true;
            } else if (lastMove != null && localX >= undoButtonX && localX <= undoButtonX + UNDO_BUTTON_WIDTH
                && localY >= buttonY && localY <= buttonY + UNDO_BUTTON_HEIGHT) {
                undoLastMove();
                return true;
            }
        }

        if (gameOver && gameWon) return true;

        int stockX = LEFT_PAD_PX;
        int stockY = TOP_PAD_PX + STATUS_BAR_HEIGHT;
        if (localX >= stockX && localX <= stockX + CARD_WIDTH && localY >= stockY && localY <= stockY + CARD_HEIGHT) {
            if (button == 0) { // left click
                if (!stock.isEmpty()) {
                    List<Card> moved = new ArrayList<>();
                    int count = drawMode.count();
                    for (int n = 0; n < count && !stock.isEmpty(); n++) {
                        Card c = stock.removeLast();
                        c.faceUp = true;
                        waste.add(c);
                        moved.add(c);
                    }

                    lastMove = new Move(moved, stock, waste);
                } else {
                    boolean moved = false;
                    while (!waste.isEmpty()) {
                        Card c = waste.removeLast();
                        c.faceUp = false;
                        stock.add(c);
                        moved = true;
                    }

                    if (moved) lastMove = null;
                    if (!hasAnyLegalMoves()) {
                        gameOver = true;
                        gameWon = false;
                        module.clearSave();
                        gameEnd = System.currentTimeMillis();
                        playSound(
                            SoundEvents.ENTITY_VILLAGER_NO,
                            rng.nextFloat(0.69f, 1.337f)
                        );
                    }

                    return true;
                }

                return true;
            } else if (button == 1) { // right click
                if (!waste.isEmpty()) {
                    Card top = waste.getLast();
                    if (tryAutoMoveToFoundations(top, waste)) {
                        waste.removeLast();
                        playSound(
                            SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                            rng.nextFloat(0.77f, 1.420f),
                            module.soundVolume.get().floatValue()
                        );
                    }
                }
                return true;
            }
        }

        int wasteY = TOP_PAD_PX + STATUS_BAR_HEIGHT;
        int wasteX = LEFT_PAD_PX + CARD_WIDTH + PILE_SPACING;
        int wasteOffset = WASTE_DRAW_3_OVERLAP * (drawMode.count() < 3 ? 0 : 3);
        if (localX >= wasteX && localX <= wasteX + CARD_WIDTH + wasteOffset && localY >= wasteY && localY <= wasteY + CARD_HEIGHT) {
            if (!waste.isEmpty() && button == 0) {
                startDragFromPile(waste, waste.size() - 1, localX, localY);
                return true;
            } else if (!waste.isEmpty() && button == 1) {
                Card top = waste.getLast();
                if (tryAutoMoveToFoundations(top, waste)) {
                    waste.removeLast();
                    playSound(
                        SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                        rng.nextFloat(0.77f, 1.420f),
                        module.soundVolume.get().floatValue()
                    );
                    return true;
                }
            }
            return false;
        }

        int foundationsStartX = LEFT_PAD_PX + 3 * (CARD_WIDTH + PILE_SPACING);

        for (int n = 0; n < FOUNDATION_COUNT; n++) {
            int fy = TOP_PAD_PX + STATUS_BAR_HEIGHT;
            int fx = foundationsStartX + n * (CARD_WIDTH + PILE_SPACING);

            List<Card> f = foundations.get(n);
            if (localX >= fx && localX <= fx + CARD_WIDTH && localY >= fy && localY <= fy + CARD_HEIGHT) {
                if (!f.isEmpty() && button == 0) {
                    startDragFromPile(f, f.size() - 1, localX, localY);
                    return true;
                }
                return false;
            }
        }

        int tableauStartY = TOP_PAD_PX
            + STATUS_BAR_HEIGHT + CARD_HEIGHT + PILE_SPACING;

        for (int n = 0; n < TABLEAU_COUNT; n++) {
            int py = tableauStartY;
            int px = LEFT_PAD_PX + n * (CARD_WIDTH + PILE_SPACING);

            List<Card> pile = tableau.get(n);
            int idx = indexAtTableauPosition(pile, (int) localX, (int) localY, px, py);

            if (idx >= 0) {
                Card c = pile.get(idx);
                if (!c.faceUp) {
                    if (idx == pile.size() - 1 && button == 0) {
                        c.faceUp = true;
                        lastMove = null;
                        return true;
                    }
                    return false;
                } else if (button == 0) {
                    startDragFromPile(pile, idx, localX, localY);
                    return true;
                } else if (button == 1) {
                    Card top = pile.getLast();
                    if (tryAutoMoveToFoundations(top, pile)) {
                        pile.removeLast();
                        playSound(
                            SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                            rng.nextFloat(0.77f, 1.420f),
                            module.soundVolume.get().floatValue()
                        );

                        tryAutoMoveAllRemainingToFoundations();

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (!dragging) return false;
        double localX = mouseX - x;
        double localY = mouseY - y;

        int foundationsStartX = LEFT_PAD_PX + 3 * (CARD_WIDTH + PILE_SPACING);

        for (int n = 0; n < FOUNDATION_COUNT; n++) {
            int fy = TOP_PAD_PX + STATUS_BAR_HEIGHT;
            int fx = foundationsStartX + n * (CARD_WIDTH + PILE_SPACING);
            if (localX >= fx && localX <= fx + CARD_WIDTH && localY >= fy && localY <= fy + CARD_HEIGHT) {
                if (canDropOnFoundation(draggedCards, foundations.get(n))) {
                    playSound(
                        SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                        rng.nextFloat(0.77f, 1.420f),
                        module.soundVolume.get().floatValue()
                    );
                    performDrop(dragOriginPile, foundations.get(n), dragOriginIndex);
                    endDrag();
                    return true;
                }
            }
        }

        int tableauStartY = TOP_PAD_PX + STATUS_BAR_HEIGHT + CARD_HEIGHT + PILE_SPACING;

        for (int pi = 0; pi < TABLEAU_COUNT; pi++) {
            int py = tableauStartY;
            int px = LEFT_PAD_PX + pi * (CARD_WIDTH + PILE_SPACING);

            int pileX1 = px;
            int pileY1 = py;
            int pileY2 = (int) height;
            int pileX2 = px + CARD_WIDTH;
            if (localX >= pileX1 && localX <= pileX2 && localY >= pileY1 && localY <= pileY2) {
                List<Card> dest = tableau.get(pi);
                if (canDropOnTableau(draggedCards, dest)) {
                    performDrop(dragOriginPile, dest, dragOriginIndex);
                    endDrag();
                    return true;
                }
            }
        }

        cancelDragReturn();
        return true;
    }

    @Override
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        super.onMouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);

        double localX = mouseX - x;
        double localY = mouseY - y;

        if (dragging) {
            this.dragX = localX;
            this.dragY = localY;
        }

        int buttonY = 3;
        int resetButtonX = (int) width - RESET_BUTTON_WIDTH - 6;
        resetButtonHovered = (localX >= resetButtonX && localX <= resetButtonX + RESET_BUTTON_WIDTH
            && localY >= buttonY && localY <= buttonY + RESET_BUTTON_HEIGHT);

        int undoButtonX = (int) width - RESET_BUTTON_WIDTH - 12 - UNDO_BUTTON_WIDTH;
        undoButtonHovered = (localX >= undoButtonX && localX <= undoButtonX + UNDO_BUTTON_WIDTH
            && localY >= buttonY && localY <= buttonY + UNDO_BUTTON_HEIGHT);
    }

    private void pollInput() {
        boolean wKey = isKeyDown(GLFW_KEY_W);
        boolean eKey = isKeyDown(GLFW_KEY_E);
        boolean rKey = isKeyDown(GLFW_KEY_R);
        boolean qKey = isKeyDown(GLFW_KEY_Q);
        boolean space = isKeyDown(GLFW_KEY_SPACE);

        if (!dragging) {
            if ((qKey && inputTracker.isNotHeld(GLFW_KEY_Q))
                || (wKey && inputTracker.isNotHeld(GLFW_KEY_W))
                || (space && inputTracker.isNotHeld(GLFW_KEY_SPACE))) {
                this.onMouseClicked(lastMouseX, lastMouseY, 0, false);
            }

            if (eKey && inputTracker.isNotHeld(GLFW_KEY_E)
                || rKey && inputTracker.isNotHeld(GLFW_KEY_R)) {
                this.onMouseClicked(lastMouseX, lastMouseY, 1, false);
            }
        } else {
            if ((qKey && inputTracker.isNotHeld(GLFW_KEY_Q))
                || (wKey && inputTracker.isNotHeld(GLFW_KEY_W))
                || (space && inputTracker.isNotHeld(GLFW_KEY_SPACE))) {
                this.onMouseReleased(lastMouseX, lastMouseY, 0);
            }

            if (eKey && inputTracker.isNotHeld(GLFW_KEY_E)
                || rKey && inputTracker.isNotHeld(GLFW_KEY_R)) {
                this.onMouseReleased(lastMouseX, lastMouseY, 1);
            }
        }

        inputTracker.updateState(GLFW_KEY_W, wKey);
        inputTracker.updateState(GLFW_KEY_E, eKey);
        inputTracker.updateState(GLFW_KEY_R, rKey);
        inputTracker.updateState(GLFW_KEY_Q, qKey);
        inputTracker.updateState(GLFW_KEY_SPACE, space);
    }

    private void initEmpty() {
        endDrag();
        stock.clear();
        waste.clear();
        lastMove = null;
        for (List<Card> t : tableau) t.clear();
        for (List<Card> f : foundations) f.clear();

        List<Card> deck = new ArrayList<>(52);

        for (Suit s : Suit.values())
            for (Rank r : Rank.values())
                deck.add(new Card(r, s));

        Collections.shuffle(deck, rng);
        for (int n = 0; n < TABLEAU_COUNT; n++) {
            List<Card> pile = tableau.get(n);
            for (int i = 0; i <= n; i++) {
                Card c = deck.removeLast();
                c.faceUp = (i == n);
                pile.add(c);
            }
        }

        while (!deck.isEmpty()) {
            Card c = deck.removeLast();
            c.faceUp = false;
            stock.add(c);
        }

        gameWon = false;
        gameOver = false;
        dragging = false;
        accumulated = 0L;
        autoMoving = false;
        dragOriginIndex = -1;
        draggedCards.clear();
        autoMoveQueue.clear();
        dragOriginPile = null;
        undoButtonHovered = false;
        resetButtonHovered = false;
        gameStart = System.currentTimeMillis();

        playSound(
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
            rng.nextFloat(0.69f, 1.337f)
        );
    }

    private void undoLastMove() {
        if (lastMove == null) return;
        List<Card> origin = lastMove.origin();
        List<Card> dest = lastMove.destination();
        List<Card> moved = new ArrayList<>(lastMove.moved());

        if (!moved.isEmpty()) {
            playSound(
                SoundEvents.ENTITY_PLAYER_TELEPORT,
                rng.nextFloat(0.42f, 0.69f)
            );
        }

        for (Card c : moved) {
            for (int n = dest.size() - 1; n >= 0; n--) {
                if (dest.get(n) == c) {
                    dest.remove(n);
                    break;
                }
            }
        }

        origin.addAll(moved);

        lastMove = null;
    }

    private void startDragFromPile(List<Card> pile, int index, double localMouseX, double localMouseY) {
        draggedCards = new ArrayList<>();
        for (int n = index; n < pile.size(); n++)
            draggedCards.add(pile.get(n));

        dragging = true;
        dragOriginPile = pile;
        dragOriginIndex = index;
        dragX = localMouseX - (CARD_WIDTH / 2.0);
        dragY = localMouseY - (CARD_HEIGHT / 2.0);

        playSound(
            SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM,
            rng.nextFloat(0.69f, 1.337f),
            module.soundVolume.get().floatValue() * 0.77f
        );
    }

    private void endDrag() {
        dragging = false;
        draggedCards.clear();
        dragOriginIndex = -1;
        dragOriginPile = null;
    }

    private void performDrop(List<Card> origin, List<Card> dest, int startIndex) {
        List<Card> moved = new ArrayList<>();
        for (int n = startIndex; n < origin.size(); n++)
            moved.add(origin.get(n));

        for (Card c : moved)
            origin.remove(c);

        dest.addAll(moved);
        lastMove = new Move(moved, origin, dest);

        playSound(
            SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM,
            rng.nextFloat(0.69f, 1.337f),
            module.soundVolume.get().floatValue() * 0.77f
        );

        if (checkWin()) {
            gameWon = true;
            gameOver = true;
            lastMove = null;
            module.clearSave();
            gameEnd = System.currentTimeMillis();
            playSound(
                SoundEvents.ENTITY_VILLAGER_CELEBRATE,
                rng.nextFloat(0.69f, 1.337f)
            );
            playSound(
                SoundEvents.BLOCK_END_PORTAL_SPAWN,
                rng.nextFloat(0.777f, 1.1337f),
                module.soundVolume.get().floatValue() * 0.42f
            );
        }
    }

    private boolean checkWin() {
        for (List<Card> f : foundations)
            if (f.size() != 13) return false;

        return true;
    }

    private boolean hasAnyLegalMoves() {
        if (!waste.isEmpty()) {
            Card wTop = waste.getLast();
            if (canDropOnAnyFoundation(wTop)) return true;
            for (List<Card> dest : tableau) {
                if (canDropOnTableau(Collections.singletonList(wTop), dest)) return true;
            }
        }

        if (!stock.isEmpty()) {
            for (int n = stock.size() - drawMode.count(); n >= 0; n -= drawMode.count()) {
                Card c = stock.get(n);
                if (canDropOnAnyFoundation(c)) return true;
                for (List<Card> tab : tableau) {
                    if (canDropOnTableau(Collections.singletonList(c), tab)) return true;
                }
            }
        }

        for (List<Card> pile : tableau) {
            if (pile.isEmpty()) continue;

            Card top = pile.getLast();
            if (top.faceUp && canDropOnAnyFoundation(top)) return true;
        }

        // If any immediately useful tableau -> tableau sequence moves exist
        // todo: maybe expand to check if partial move can free a card for the foundation ( + hints?)
        for (List<Card> src : tableau) {
            boolean foundFaceDown = false;
            for (int idx = 0; idx < src.size(); idx++) {
                Card c = src.get(idx);
                if (!c.faceUp) continue;
                if (!foundFaceDown) continue; // ignore tableau stacks which can't reveal any face-down cards
                List<Card> faceUpStack = src.subList(idx, src.size());

                for (List<Card> dest : tableau) {
                    if (dest == src) continue;
                    if (canDropOnTableau(new ArrayList<>(faceUpStack), dest)) return true;
                }
            }
        }

        return false;
    }

    private boolean canDropOnAnyFoundation(Card c) {
        for (List<Card> f : foundations)
            if (canDropOnFoundation(Collections.singletonList(c), f))
                return true;

        return false;
    }

    private boolean canDropOnFoundation(List<Card> moving, List<Card> dest) {
        if (moving.isEmpty()) return false;
        if (moving.size() > 1) return false;

        Card c = moving.getFirst();
        if (dest.isEmpty()) return c.rank == Rank.ACE;

        Card top = dest.getLast();
        return top.suit == c.suit && top.rank.ordinal() + 1 == c.rank.ordinal();
    }

    private boolean canDropOnTableau(List<Card> moving, List<Card> dest) {
        if (moving.isEmpty()) return false;

        Card first = moving.getFirst();
        if (dest.isEmpty()) return first.rank == Rank.KING;

        Card top = dest.getLast();
        if (!top.faceUp) return false;

        boolean colorsOpposite = top.suit.isRed() != first.suit.isRed();
        boolean rankCorrect = top.rank.ordinal() == first.rank.ordinal() + 1;

        return colorsOpposite && rankCorrect;
    }

    private void tryAutoMoveAllRemainingToFoundations() {
        if (!stock.isEmpty() || !waste.isEmpty()) return;
        for (List<Card> pile : tableau) for (Card c : pile) if (!c.faceUp) return;

        List<List<Card>> simFoundations = new ArrayList<>();
        for (List<Card> f : foundations) simFoundations.add(new ArrayList<>(f));

        int count = tableau.size();
        int[] sizes = new int[count];
        for (int n = 0; n < count; n++) sizes[n] = tableau.get(n).size();

        Deque<AutoMove> planned = new ArrayDeque<>();

        boolean progress = true;
        while (progress) {
            progress = false;
            for (int n = 0; n < count; n++) {
                if (sizes[n] == 0) continue;
                Card top = tableau.get(n).get(sizes[n] - 1);
                if (canDropOnAnySimulatedFoundation(top, simFoundations)) {
                    int idx = findSimulatedFoundationIndexFor(top, simFoundations);
                    if (idx >= 0) {
                        planned.addLast(new AutoMove(n, top));
                        simFoundations.get(idx).add(top);
                        sizes[n]--;
                        progress = true;
                    }
                }
            }
        }

        if (planned.isEmpty()) return;

        autoMoving = true;
        autoMoveQueue.addAll(planned);
        nextAutoMoveTime = System.currentTimeMillis() + AUTO_MOVE_INTERVAL;
    }

    private boolean canDropOnAnySimulatedFoundation(Card c, List<List<Card>> simFoundations) {
        for (List<Card> f : simFoundations)
            if (canDropOnFoundation(Collections.singletonList(c), f))
                return true;

        return false;
    }

    private int findSimulatedFoundationIndexFor(Card c, List<List<Card>> simFoundations) {
        for (int n = 0; n < simFoundations.size(); n++) {
            List<Card> f = simFoundations.get(n);
            if (canDropOnFoundation(Collections.singletonList(c), f))
                return n;
        }

        return -1;
    }


    private boolean tryAutoMoveToFoundations(Card c, List<Card> origin) {
        for (List<Card> f : foundations) {
            if (canDropOnFoundation(Collections.singletonList(c), f)) {
                f.add(c);
                lastMove = new Move(Collections.singletonList(c), origin, f);
                if (checkWin()) {
                    gameWon = true;
                    gameOver = true;
                    lastMove = null;
                    module.clearSave();
                    gameEnd = System.currentTimeMillis();
                    playSound(
                        SoundEvents.ENTITY_VILLAGER_CELEBRATE,
                        rng.nextFloat(0.69f, 1.337f)
                    );
                    playSound(
                        SoundEvents.BLOCK_END_PORTAL_SPAWN,
                        rng.nextFloat(0.777f, 1.1337f),
                        module.soundVolume.get().floatValue() * 0.42f
                    );
                }
                return true;
            }
        }
        return false;
    }

    private int indexAtTableauPosition(List<Card> pile, int localX, int localY, int px, int pyStart) {
        if (pile.isEmpty()) {
            if (localX >= px && localX <= px + CARD_WIDTH && localY >= pyStart && localY <= pyStart + CARD_HEIGHT) {
                return -1; // empty pile
            } else {
                return -2; // outside pile
            }
        }

        int size = pile.size();
        int[] yPos = new int[size];

        int curY = pyStart;
        for (int n = 0; n < size; n++) {
            yPos[n] = curY;
            Card c = pile.get(n);
            curY += (c.faceUp ? TAB_OVERLAP_FACEUP : TAB_OVERLAP_FACEDOWN);
        }

        for (int n = size - 1; n >= 0; n--) {
            int top = yPos[n];
            int bottom = top + CARD_HEIGHT;
            if (localX >= px && localX <= px + CARD_WIDTH && localY >= top && localY <= bottom) {
                return n;
            }
        }

        int lastTop = yPos[size - 1];
        if (localX >= px && localX <= px + CARD_WIDTH && localY >= lastTop && localY <= lastTop + CARD_HEIGHT) {
            return size - 1;
        }

        return -2; // outside pile
    }

    public void cancelDragReturn() {
        playSound(
            SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM,
            rng.nextFloat(0.69f, 1.337f),
            module.soundVolume.get().floatValue() * 0.69f
        );
        endDrag();
    }

    public boolean shouldSaveGame() {
        return module.shouldSave.get() && !gameOver;
    }

    public SaveState saveGame() {
        return new SaveState(
            drawMode, stock, waste, foundations, tableau, accumulated + (System.currentTimeMillis() - gameStart)
        );
    }

    public void playSound(SoundEvent sound, float pitch) {
        if (!module.sounds.get()) return;
        try {
            if (mc == null) return;
            if (mc.getSoundManager() == null) return;
            mc.getSoundManager().play(PositionedSoundInstance.master(sound, pitch, module.soundVolume.get().floatValue()));
        } catch (Throwable ignored) {}
    }

    public void playSound(SoundEvent sound, float pitch, float volume) {
        if (!module.sounds.get()) return;
        try {
            if (mc == null) return;
            if (mc.getSoundManager() == null) return;
            mc.getSoundManager().play(PositionedSoundInstance.master(sound, pitch, volume));
        } catch (Throwable ignored) {}
    }

    private record AutoMove(int fromPileIdx, Card card) {}
    private record Move(List<Card> moved, List<Card> origin, List<Card> destination) {}
}

