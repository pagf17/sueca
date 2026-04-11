package sueca.ui;

import sueca.ai.AIPlayer;
import sueca.model.*;
import sueca.ui.theme.GameTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    public interface PauseListener {
        void onToggleFullscreen();
        void onGoToMenu();
        void onExitGame();
        boolean isFullscreen();
        void onCycleTheme();
        String getCurrentThemeName();
    }

    private GameEngine engine;
    private GameTheme theme;
    private Difficulty difficulty;
    private PauseListener pauseListener;

    private int hoveredCardIndex = -1;
    private boolean waitingForHuman = false;
    private boolean animating = false;

    // Played cards positions (for the trick display)
    private Card[] trickCards = new Card[4];   // cards in current trick display
    private int[] trickPlayerIdx = new int[4]; // which player played each
    private int trickDisplayCount = 0;

    // All cards played so far (for AI memory)
    private List<Card> allPlayedCards = new ArrayList<>();

    // Trick completion display timer
    private Timer trickCompleteTimer;
    private boolean showingCompleteTrick = false;

    // AI play timer
    private Timer aiTimer;

    // Message to display
    private String statusMessage = "";

    // Pause menu
    private boolean paused = false;
    private Rectangle[] pauseMenuButtons = new Rectangle[5];
    private int pauseHoverIndex = -1;

    // Round/match result overlay
    private boolean showingRoundResult = false;
    private boolean resultIsMatchOver = false;
    private Rectangle[] resultButtons = new Rectangle[2];
    private int resultHoverIndex = -1;

    // Rendering caches — avoid recreating heavy objects every repaint
    private static final Font FONT_WATERMARK   = new Font(CardRenderer.SUIT_FONT_NAME, Font.PLAIN, 120);
    private static final Font FONT_TRUMP_SUIT  = new Font(CardRenderer.SUIT_FONT_NAME, Font.PLAIN, 28);
    private static final Font FONT_SCOREBOARD_DOTS = new Font("SansSerif", Font.BOLD, 11);
    private static final Color SHADOW_50 = new Color(0, 0, 0, 50);
    private static final Color WATERMARK_COLOR = new Color(0, 0, 0, 15);
    private static final Color SEPARATOR_COLOR = new Color(180, 180, 180);
    private static final Color DOT_EMPTY_COLOR = new Color(180, 180, 180);
    private static final Color DOT_RING_COLOR  = new Color(140, 140, 140);

    private RadialGradientPaint cachedGradient;
    private GameTheme cachedGradientTheme;
    private int cachedGradW = -1, cachedGradH = -1;

    private Path2D cachedOctagon, cachedInnerOctagon;
    private Color cachedInnerFelt;
    private GameTheme cachedTableTheme;
    private int cachedTableW = -1, cachedTableH = -1;

    public GamePanel(GameEngine engine, GameTheme theme, Difficulty difficulty, PauseListener pauseListener) {
        this.engine = engine;
        this.theme = theme;
        this.difficulty = difficulty;
        this.pauseListener = pauseListener;
        setPreferredSize(new Dimension(1024, 700));
        addMouseListener(this);
        addMouseMotionListener(this);
        setDoubleBuffered(true);

        for (int i = 0; i < 5; i++) pauseMenuButtons[i] = new Rectangle();
        for (int i = 0; i < 2; i++) resultButtons[i] = new Rectangle();

        // Escape key toggles pause
        getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "togglePause");
        getActionMap().put("togglePause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { togglePause(); }
        });
    }

    public void setTheme(GameTheme theme) {
        this.theme = theme;
        // Invalidate theme-dependent caches
        cachedGradientTheme = null;
        cachedTableTheme = null;
        CardRenderer.invalidateCardBackCache();
        repaint();
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void stopTimers() {
        if (aiTimer != null) aiTimer.stop();
        if (trickCompleteTimer != null) trickCompleteTimer.stop();
    }

    private void togglePause() {
        paused = !paused;
        pauseHoverIndex = -1;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    public void startRound() {
        showingRoundResult = false;
        resultHoverIndex = -1;
        engine.newRound();
        allPlayedCards.clear();
        trickCards = new Card[4];
        trickDisplayCount = 0;
        showingCompleteTrick = false;
        statusMessage = "";
        repaint();

        checkCurrentPlayer();
    }

    private void checkCurrentPlayer() {
        if (engine.isRoundOver()) {
            showingRoundResult = true;
            resultIsMatchOver = engine.isMatchOver();
            repaint();
            return;
        }

        Player current = engine.getCurrentPlayer();
        if (current.isHuman()) {
            waitingForHuman = true;
            repaint();
        } else {
            waitingForHuman = false;
            playAI();
        }
    }

    private void playAI() {
        animating = true;
        aiTimer = new Timer(600, e -> {
            Player current = engine.getCurrentPlayer();
            Card card = AIPlayer.chooseCard(current, engine.getCurrentTrick(),
                    engine.getTrump(), engine.getTeamPoints(), difficulty, allPlayedCards);

            if (card != null) {
                executePlay(current, card);
            }
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    private void executePlay(Player player, Card card) {
        int pos = player.getIndex();
        trickCards[pos] = card;
        trickDisplayCount++;
        allPlayedCards.add(card);

        engine.playCard(player, card);
        animating = false;
        repaint();

        // Check if trick complete
        if (trickDisplayCount == 4) {
            showingCompleteTrick = true;
            repaint();

            trickCompleteTimer = new Timer(1200, e -> {
                showingCompleteTrick = false;
                trickDisplayCount = 0;
                trickCards = new Card[4];

                if (!engine.isRoundOver()) {
                    engine.startNewTrick();
                }
                repaint();

                // Small delay then continue
                Timer next = new Timer(200, ev -> checkCurrentPlayer());
                next.setRepeats(false);
                next.start();
            });
            trickCompleteTimer.setRepeats(false);
            trickCompleteTimer.start();
        } else {
            checkCurrentPlayer();
        }
    }

    // ─── PAINTING ───

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();

        drawBackground(g2, w, h);
        drawTable(g2, w, h);
        drawScoreboard(g2);
        drawTrumpIndicator(g2, w);
        drawOpponentHands(g2, w, h);
        drawPlayerHand(g2, w, h);
        drawPlayedCards(g2, w, h);
        drawPlayerLabels(g2, w, h);
        drawStatus(g2, w, h);

        if (showingRoundResult) drawResultOverlay(g2, w, h);
        if (paused) drawPauseOverlay(g2, w, h);
    }

    private void drawBackground(Graphics2D g2, int w, int h) {
        // Rebuild gradient only when size or theme changes
        if (cachedGradient == null || w != cachedGradW || h != cachedGradH || theme != cachedGradientTheme) {
            Point2D center = new Point2D.Float(w / 2f, h / 2f);
            float radius = Math.max(w, h) * 0.7f;
            Color[] colors = {theme.getFeltColor(), theme.getFeltDarker()};
            float[] dist = {0f, 1f};
            cachedGradient = new RadialGradientPaint(center, radius, dist, colors);
            cachedGradW = w;
            cachedGradH = h;
            cachedGradientTheme = theme;
        }
        g2.setPaint(cachedGradient);
        g2.fillRect(0, 0, w, h);

        // Subtle suit watermarks in corners
        g2.setFont(FONT_WATERMARK);
        g2.setColor(WATERMARK_COLOR);
        g2.drawString("♣", 20, 130);
        g2.drawString("♠", w - 130, 130);
        g2.drawString("♦", 20, h - 20);
        g2.drawString("♥", w - 130, h - 20);
    }

    private static final BasicStroke STROKE_TABLE_BORDER = new BasicStroke(5f);
    private static final BasicStroke STROKE_TABLE_INNER  = new BasicStroke(2f);
    private static final Color TABLE_INNER_SHADOW = new Color(0, 0, 0, 30);

    private void drawTable(Graphics2D g2, int w, int h) {
        int tableW = 420;
        int tableH = 280;
        int cx = w / 2;
        int cy = h / 2 - 30;

        // Rebuild octagon paths only when size or theme changes
        if (cachedOctagon == null || w != cachedTableW || h != cachedTableH || theme != cachedTableTheme) {
            cachedOctagon = createOctagon(cx, cy, tableW, tableH);
            cachedInnerOctagon = createOctagon(cx, cy, tableW - 20, tableH - 16);
            Color felt = theme.getFeltColor();
            cachedInnerFelt = new Color(
                    Math.min(255, felt.getRed() + 10),
                    Math.min(255, felt.getGreen() + 15),
                    Math.min(255, felt.getBlue() + 10)
            );
            cachedTableW = w;
            cachedTableH = h;
            cachedTableTheme = theme;
        }

        // Border
        g2.setColor(theme.getTableBorderColor());
        g2.setStroke(STROKE_TABLE_BORDER);
        g2.draw(cachedOctagon);

        // Fill
        g2.setColor(cachedInnerFelt);
        g2.fill(cachedOctagon);

        // Inner shadow
        g2.setColor(TABLE_INNER_SHADOW);
        g2.setStroke(STROKE_TABLE_INNER);
        g2.draw(cachedInnerOctagon);
    }

    private Path2D createOctagon(int cx, int cy, int w, int h) {
        Path2D path = new Path2D.Double();
        double hw = w / 2.0, hh = h / 2.0;
        double cut = 0.3;
        path.moveTo(cx - hw + hw * cut, cy - hh);
        path.lineTo(cx + hw - hw * cut, cy - hh);
        path.lineTo(cx + hw, cy - hh + hh * cut);
        path.lineTo(cx + hw, cy + hh - hh * cut);
        path.lineTo(cx + hw - hw * cut, cy + hh);
        path.lineTo(cx - hw + hw * cut, cy + hh);
        path.lineTo(cx - hw, cy + hh - hh * cut);
        path.lineTo(cx - hw, cy - hh + hh * cut);
        path.closePath();
        return path;
    }

    private void drawScoreboard(Graphics2D g2) {
        int x = 12, y = 12, w = 175, h = 105;

        // Panel background
        g2.setColor(theme.getPanelBackground());
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 14, 14));
        g2.setColor(theme.getPanelBorder());
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 14, 14));

        g2.setFont(theme.getScoreFont());
        g2.setColor(theme.getTextColor());

        // Team names and points
        int[] pts = engine.getTeamPoints();
        int[] games = engine.getGamesWon();

        g2.drawString("Nós:", x + 12, y + 25);
        g2.setColor(theme.getLabelTeamUs());
        g2.drawString(pts != null ? pts[0] + " pts" : "0 pts", x + 90, y + 25);

        g2.setColor(theme.getTextColor());
        g2.drawString("Eles:", x + 12, y + 48);
        g2.setColor(theme.getLabelTeamThem());
        g2.drawString(pts != null ? pts[1] + " pts" : "0 pts", x + 90, y + 48);

        // Separator
        g2.setColor(SEPARATOR_COLOR);
        g2.drawLine(x + 10, y + 58, x + w - 10, y + 58);

        // Games won dots
        g2.setFont(FONT_SCOREBOARD_DOTS);
        g2.setColor(theme.getTextColor());
        g2.drawString("Jogos:", x + 12, y + 76);

        for (int i = 0; i < 4; i++) {
            int dotX = x + 80 + i * 16;
            g2.setColor(i < games[0] ? theme.getAccentColor() : DOT_EMPTY_COLOR);
            g2.fillOval(dotX, y + 67, 10, 10);
            g2.setColor(DOT_RING_COLOR);
            g2.drawOval(dotX, y + 67, 10, 10);
        }

        // Eles games
        g2.setColor(theme.getTextColor());
        g2.drawString("      ", x + 12, y + 96);

        for (int i = 0; i < 4; i++) {
            int dotX = x + 80 + i * 16;
            g2.setColor(i < games[1] ? theme.getLabelTeamThem() : DOT_EMPTY_COLOR);
            g2.fillOval(dotX, y + 87, 10, 10);
            g2.setColor(DOT_RING_COLOR);
            g2.drawOval(dotX, y + 87, 10, 10);
        }
    }

    private void drawTrumpIndicator(Graphics2D g2, int w) {
        if (engine.getTrump() == null) return;

        int px = w - 130, py = 12, pw = 115, ph = 45;

        g2.setColor(theme.getPanelBackground());
        g2.fill(new RoundRectangle2D.Double(px, py, pw, ph, 14, 14));
        g2.setColor(theme.getPanelBorder());
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(px, py, pw, ph, 14, 14));

        g2.setFont(theme.getLabelFont());
        g2.setColor(theme.getTextColor());
        g2.drawString("Trunfo:", px + 10, py + 30);

        Color suitColor = engine.getTrump().isRed() ? theme.getRedColor() : theme.getBlackColor();
        g2.setColor(suitColor);
        g2.setFont(FONT_TRUMP_SUIT);
        g2.drawString(engine.getTrump().getSymbol(), px + 75, py + 34);
    }

    private void drawPlayerLabels(Graphics2D g2, int w, int h) {
        Player[] players = engine.getPlayers();
        int cx = w / 2, cy = h / 2 - 30;

        // Bottom (human)
        drawLabel(g2, players[0].getName(), cx, h - CardRenderer.CARD_H - 35, players[0].getTeam(),
                engine.getCurrentPlayerIndex() == 0 && waitingForHuman);
        // Top (partner)
        drawLabel(g2, players[2].getName(), cx, cy - 160, players[2].getTeam(),
                engine.getCurrentPlayerIndex() == 2);
        // Left
        drawLabel(g2, players[3].getName(), 105, cy + 35, players[3].getTeam(),
                engine.getCurrentPlayerIndex() == 3);
        // Right
        drawLabel(g2, players[1].getName(), w - 105, cy + 35, players[1].getTeam(),
                engine.getCurrentPlayerIndex() == 1);
    }

    private void drawLabel(Graphics2D g2, String name, int x, int y, int team, boolean active) {
        g2.setFont(theme.getLabelFont());
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(name);
        int pad = 14;

        Color bg = team == 0 ? theme.getLabelTeamUs() : theme.getLabelTeamThem();
        if (active) bg = bg.brighter();

        g2.setColor(SHADOW_50);
        g2.fill(new RoundRectangle2D.Double(x - tw / 2.0 - pad + 2, y - 10 + 2, tw + pad * 2, 26, 8, 8));

        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(x - tw / 2.0 - pad, y - 10, tw + pad * 2, 26, 8, 8));

        if (active) {
            g2.setColor(new Color(255, 255, 255, 80));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(x - tw / 2.0 - pad, y - 10, tw + pad * 2, 26, 8, 8));
        }

        g2.setColor(Color.WHITE);
        g2.drawString(name, x - tw / 2, y + 7);
    }

    private void drawOpponentHands(Graphics2D g2, int w, int h) {
        Player[] players = engine.getPlayers();
        int cx = w / 2, cy = h / 2 - 30;

        // Top player (index 2) - horizontal spread
        int topCards = players[2].getHand().size();
        int topStartX = cx - (topCards * 25) / 2;
        for (int i = 0; i < topCards; i++) {
            CardRenderer.drawCardBack(g2, topStartX + i * 25, 10, theme);
        }

        // Left player (index 3) - vertical spread
        int leftCards = players[3].getHand().size();
        int leftStartY = cy - (leftCards * 22) / 2;
        for (int i = 0; i < leftCards; i++) {
            Graphics2D g2r = (Graphics2D) g2.create();
            int cardX = 15;
            int cardY = leftStartY + i * 22;
            g2r.rotate(Math.PI / 2, cardX + CardRenderer.CARD_W / 2.0, cardY + CardRenderer.CARD_H / 2.0);
            CardRenderer.drawCardBack(g2r, cardX, cardY, theme);
            g2r.dispose();
        }

        // Right player (index 1) - vertical spread
        int rightCards = players[1].getHand().size();
        int rightStartY = cy - (rightCards * 22) / 2;
        for (int i = 0; i < rightCards; i++) {
            Graphics2D g2r = (Graphics2D) g2.create();
            int cardX = w - CardRenderer.CARD_W - 15;
            int cardY = rightStartY + i * 22;
            g2r.rotate(-Math.PI / 2, cardX + CardRenderer.CARD_W / 2.0, cardY + CardRenderer.CARD_H / 2.0);
            CardRenderer.drawCardBack(g2r, cardX, cardY, theme);
            g2r.dispose();
        }
    }

    private void drawPlayerHand(Graphics2D g2, int w, int h) {
        Player human = engine.getPlayers()[0];
        List<Card> hand = human.getHand();
        int count = hand.size();
        if (count == 0) return;

        int spacing = Math.min(58, (w - 200) / count);
        int totalW = (count - 1) * spacing + CardRenderer.CARD_W;
        int startX = (w - totalW) / 2;
        int baseY = h - CardRenderer.CARD_H - 15;

        Suit leadSuit = engine.getLeadSuit();
        List<Card> playable = human.getPlayableCards(leadSuit);

        for (int i = 0; i < count; i++) {
            Card card = hand.get(i);
            int x = startX + i * spacing;
            int y = baseY;
            boolean canPlay = waitingForHuman && playable.contains(card);
            boolean hovered = (i == hoveredCardIndex) && canPlay;

            if (hovered) {
                y -= 20;
            }

            if (!waitingForHuman || !canPlay) {
                // Draw slightly dimmed
                Composite orig = g2.getComposite();
                if (waitingForHuman && !canPlay) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                }
                CardRenderer.drawCard(g2, card, x, y, theme, false);
                g2.setComposite(orig);
            } else {
                CardRenderer.drawCard(g2, card, x, y, theme, hovered);
            }
        }
    }

    private void drawPlayedCards(Graphics2D g2, int w, int h) {
        int cx = w / 2;
        int cy = h / 2 - 30;

        // Positions for each player's played card
        int[][] offsets = {
                {cx - CardRenderer.CARD_W / 2, cy + 30},                 // bottom (player 0)
                {cx + 60, cy - CardRenderer.CARD_H / 2},                 // right (player 1)
                {cx - CardRenderer.CARD_W / 2, cy - CardRenderer.CARD_H - 20}, // top (player 2)
                {cx - CardRenderer.CARD_W - 60, cy - CardRenderer.CARD_H / 2}  // left (player 3)
        };

        for (int i = 0; i < 4; i++) {
            if (trickCards[i] != null) {
                CardRenderer.drawCard(g2, trickCards[i], offsets[i][0], offsets[i][1], theme, false);
            }
        }
    }

    private void drawStatus(Graphics2D g2, int w, int h) {
        if (statusMessage != null && !statusMessage.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int sw = fm.stringWidth(statusMessage);

            int px = (w - sw) / 2 - 20;
            int py = h / 2 - 55;

            g2.setColor(new Color(0, 0, 0, 160));
            g2.fill(new RoundRectangle2D.Double(px, py - 18, sw + 40, 36, 12, 12));

            g2.setColor(Color.WHITE);
            g2.drawString(statusMessage, px + 20, py + 5);
        }
    }

    private void drawResultOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, w, h);

        int panelW = 460, panelH = 320;
        int px = (w - panelW) / 2;
        int py = (h - panelH) / 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fill(new RoundRectangle2D.Double(px + 6, py + 6, panelW, panelH, 20, 20));

        // Panel
        g2.setColor(new Color(25, 70, 25));
        g2.fill(new RoundRectangle2D.Double(px, py, panelW, panelH, 20, 20));
        g2.setColor(new Color(107, 58, 31));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(new RoundRectangle2D.Double(px, py, panelW, panelH, 20, 20));

        FontMetrics fm;
        String title = resultIsMatchOver ? "FIM DA PARTIDA" : "FIM DA RONDA";
        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        g2.drawString(title, px + (panelW - fm.stringWidth(title)) / 2, py + 48);

        g2.setColor(new Color(107, 58, 31));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(px + 20, py + 60, px + panelW - 20, py + 60);

        // Result text
        String resultText = resultIsMatchOver ? engine.getMatchResult() : engine.getRoundResult();
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(255, 220, 100));
        fm = g2.getFontMetrics();
        g2.drawString(resultText, px + (panelW - fm.stringWidth(resultText)) / 2, py + 92);

        // Points this round
        int[] pts = engine.getTeamPoints();
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.setColor(Color.WHITE);
        String pointsLine = "Pontos: Nos " + pts[0] + "  —  Eles " + pts[1];
        fm = g2.getFontMetrics();
        g2.drawString(pointsLine, px + (panelW - fm.stringWidth(pointsLine)) / 2, py + 116);

        // Games won row
        int[] games = engine.getGamesWon();
        int dotSize = 14, dotGap = 6;
        int dotsW = 4 * (dotSize + dotGap) - dotGap;

        // Nos row
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String nosLabel = "Nos:  ";
        fm = g2.getFontMetrics();
        int rowX = px + (panelW - fm.stringWidth(nosLabel) - dotsW - 80) / 2;
        int row1Y = py + 148;
        g2.setColor(Color.WHITE);
        g2.drawString(nosLabel, rowX, row1Y);
        int dotsStartX = rowX + fm.stringWidth(nosLabel);
        for (int i = 0; i < 4; i++) {
            g2.setColor(i < games[0] ? new Color(100, 210, 130) : new Color(80, 80, 80));
            g2.fillOval(dotsStartX + i * (dotSize + dotGap), row1Y - dotSize + 2, dotSize, dotSize);
            g2.setColor(new Color(150, 150, 150));
            g2.drawOval(dotsStartX + i * (dotSize + dotGap), row1Y - dotSize + 2, dotSize, dotSize);
        }

        // Eles row
        String elesLabel = "Eles: ";
        int row2Y = row1Y + 26;
        int elesLabelX = rowX;
        g2.setColor(Color.WHITE);
        g2.drawString(elesLabel, elesLabelX, row2Y);
        int elesDotsX = elesLabelX + fm.stringWidth(elesLabel);
        for (int i = 0; i < 4; i++) {
            g2.setColor(i < games[1] ? new Color(220, 100, 100) : new Color(80, 80, 80));
            g2.fillOval(elesDotsX + i * (dotSize + dotGap), row2Y - dotSize + 2, dotSize, dotSize);
            g2.setColor(new Color(150, 150, 150));
            g2.drawOval(elesDotsX + i * (dotSize + dotGap), row2Y - dotSize + 2, dotSize, dotSize);
        }

        // Buttons
        int btnW = 190, btnH = 46, btnGap = 16;
        int totalBtns = btnW * 2 + btnGap;
        int btn0x = px + (panelW - totalBtns) / 2;
        int btn1x = btn0x + btnW + btnGap;
        int btnY = py + panelH - btnH - 22;

        String btn0Label = resultIsMatchOver ? "Nova Partida" : "Proxima Ronda";
        String btn1Label = "Menu Principal";
        Color[] btnColors = {new Color(50, 185, 90), new Color(65, 145, 240)};
        int[] btnXs = {btn0x, btn1x};
        String[] btnLabels = {btn0Label, btn1Label};

        for (int i = 0; i < 2; i++) {
            resultButtons[i] = new Rectangle(btnXs[i], btnY, btnW, btnH);
            boolean hover = resultHoverIndex == i;
            Color bc = hover ? btnColors[i].brighter() : btnColors[i];

            g2.setColor(new Color(0, 0, 0, 60));
            g2.fill(new RoundRectangle2D.Double(btnXs[i] + 3, btnY + 3, btnW, btnH, 12, 12));
            g2.setColor(bc);
            g2.fill(new RoundRectangle2D.Double(btnXs[i], btnY, btnW, btnH, 12, 12));
            if (hover) {
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(btnXs[i], btnY, btnW, btnH, 12, 12));
            }
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(Color.WHITE);
            fm = g2.getFontMetrics();
            g2.drawString(btnLabels[i], btnXs[i] + (btnW - fm.stringWidth(btnLabels[i])) / 2, btnY + 30);
        }
    }

    private void drawPauseOverlay(Graphics2D g2, int w, int h) {
        // Semi-transparent backdrop
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, w, h);

        int panelW = 320, panelH = 410;
        int px = (w - panelW) / 2;
        int py = (h - panelH) / 2;

        // Panel shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fill(new RoundRectangle2D.Double(px + 6, py + 6, panelW, panelH, 20, 20));

        // Panel background
        g2.setColor(new Color(25, 70, 25));
        g2.fill(new RoundRectangle2D.Double(px, py, panelW, panelH, 20, 20));

        // Panel border
        g2.setColor(new Color(107, 58, 31));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(new RoundRectangle2D.Double(px, py, panelW, panelH, 20, 20));

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 34));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String title = "PAUSA";
        g2.drawString(title, px + (panelW - fm.stringWidth(title)) / 2, py + 55);

        // Separator
        g2.setColor(new Color(107, 58, 31));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(px + 20, py + 68, px + panelW - 20, py + 68);

        // Buttons
        String themeName = pauseListener.getCurrentThemeName();
        String[] labels = {
            "Continuar",
            "Tema: " + themeName + " >",
            pauseListener.isFullscreen() ? "Modo Janela" : "Ecra Inteiro",
            "Menu Principal",
            "Sair do Jogo"
        };
        Color[] btnColors = {
            new Color(50, 185, 90),
            new Color(130, 80, 200),
            new Color(65, 145, 240),
            new Color(65, 145, 240),
            new Color(170, 40, 40)
        };

        int btnW = 260, btnH = 46, btnGap = 10;
        int btnX = px + (panelW - btnW) / 2;
        int btnStartY = py + 82;

        for (int i = 0; i < 5; i++) {
            int by = btnStartY + i * (btnH + btnGap);
            pauseMenuButtons[i] = new Rectangle(btnX, by, btnW, btnH);

            boolean hover = (pauseHoverIndex == i);
            Color bc = hover ? btnColors[i].brighter() : btnColors[i];

            // Shadow
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fill(new RoundRectangle2D.Double(btnX + 3, by + 3, btnW, btnH, 12, 12));

            g2.setColor(bc);
            g2.fill(new RoundRectangle2D.Double(btnX, by, btnW, btnH, 12, 12));

            if (hover) {
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(btnX, by, btnW, btnH, 12, 12));
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 17));
            g2.setColor(Color.WHITE);
            fm = g2.getFontMetrics();
            g2.drawString(labels[i], btnX + (btnW - fm.stringWidth(labels[i])) / 2, by + 30);
        }

        // Hint
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(255, 255, 255, 100));
        String hint = "Esc para continuar";
        fm = g2.getFontMetrics();
        g2.drawString(hint, px + (panelW - fm.stringWidth(hint)) / 2, py + panelH - 12);
    }

    // ─── MOUSE EVENTS ───

    private void handleResultClick(int x, int y) {
        if (resultButtons[0].contains(x, y)) {
            showingRoundResult = false;
            if (resultIsMatchOver) engine.newMatch();
            startRound();
        } else if (resultButtons[1].contains(x, y)) {
            showingRoundResult = false;
            stopTimers();
            pauseListener.onGoToMenu();
        }
    }

    private void updateResultHover(int x, int y) {
        int old = resultHoverIndex;
        resultHoverIndex = -1;
        for (int i = 0; i < 2; i++) {
            if (resultButtons[i].contains(x, y)) { resultHoverIndex = i; break; }
        }
        if (old != resultHoverIndex) {
            setCursor(resultHoverIndex >= 0
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            repaint();
        }
    }

    private void handlePauseClick(int x, int y) {
        for (int i = 0; i < 5; i++) {
            if (pauseMenuButtons[i].contains(x, y)) {
                switch (i) {
                    case 0: // Continuar
                        togglePause();
                        break;
                    case 1: // Trocar Tema
                        pauseListener.onCycleTheme();
                        repaint();
                        break;
                    case 2: // Ecra Inteiro / Modo Janela
                        pauseListener.onToggleFullscreen();
                        repaint();
                        break;
                    case 3: // Menu Principal
                        paused = false;
                        stopTimers();
                        pauseListener.onGoToMenu();
                        break;
                    case 4: // Sair do Jogo
                        pauseListener.onExitGame();
                        break;
                }
                return;
            }
        }
    }

    private void updatePauseHover(int x, int y) {
        int old = pauseHoverIndex;
        pauseHoverIndex = -1;
        for (int i = 0; i < 5; i++) {
            if (pauseMenuButtons[i].contains(x, y)) {
                pauseHoverIndex = i;
                break;
            }
        }
        if (old != pauseHoverIndex) {
            setCursor(pauseHoverIndex >= 0
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            repaint();
        }
    }

    private int getCardIndexAtPoint(int mx, int my) {
        Player human = engine.getPlayers()[0];
        List<Card> hand = human.getHand();
        int count = hand.size();
        if (count == 0) return -1;

        int w = getWidth(), h = getHeight();
        int spacing = Math.min(58, (w - 200) / count);
        int totalW = (count - 1) * spacing + CardRenderer.CARD_W;
        int startX = (w - totalW) / 2;
        int baseY = h - CardRenderer.CARD_H - 15;

        // Check from right to left (top card first)
        for (int i = count - 1; i >= 0; i--) {
            int x = startX + i * spacing;
            int y = baseY;
            if (i == hoveredCardIndex) y -= 20;

            int cardRight = (i == count - 1) ? x + CardRenderer.CARD_W : startX + (i + 1) * spacing;

            if (mx >= x && mx <= cardRight && my >= y && my <= y + CardRenderer.CARD_H) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (paused) {
            handlePauseClick(e.getX(), e.getY());
            return;
        }
        if (showingRoundResult) {
            handleResultClick(e.getX(), e.getY());
            return;
        }
        if (!waitingForHuman || animating || showingCompleteTrick) return;

        int idx = getCardIndexAtPoint(e.getX(), e.getY());
        if (idx < 0) return;

        Player human = engine.getPlayers()[0];
        Card card = human.getHand().get(idx);

        Suit leadSuit = engine.getLeadSuit();
        List<Card> playable = human.getPlayableCards(leadSuit);

        if (playable.contains(card)) {
            waitingForHuman = false;
            hoveredCardIndex = -1;
            executePlay(human, card);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (paused) {
            updatePauseHover(e.getX(), e.getY());
            return;
        }
        if (showingRoundResult) {
            updateResultHover(e.getX(), e.getY());
            return;
        }
        int oldHover = hoveredCardIndex;
        hoveredCardIndex = getCardIndexAtPoint(e.getX(), e.getY());
        if (oldHover != hoveredCardIndex) {
            repaint();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {
        if (!paused) { hoveredCardIndex = -1; repaint(); }
    }
    @Override public void mouseDragged(MouseEvent e) {}
}
