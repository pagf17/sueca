package sueca.ui;

import sueca.model.Card;
import sueca.ui.theme.GameTheme;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class CardRenderer {

    public static final int CARD_W = 70;
    public static final int CARD_H = 100;

    /**
     * Finds a font family that can render suit symbols (♥ ♦ ♣ ♠).
     * On Linux the default "SansSerif" logical font often lacks these glyphs;
     * DejaVu Sans / Noto Sans are reliably present on most distributions.
     */
    public static final String SUIT_FONT_NAME = findSuitFontName();

    private static String findSuitFontName() {
        String[] candidates = {"DejaVu Sans", "Noto Sans", "Liberation Sans", "Dialog", "SansSerif"};
        for (String name : candidates) {
            Font f = new Font(name, Font.PLAIN, 12);
            if (f.canDisplay('\u2660') && f.canDisplay('\u2665')) {
                return name;
            }
        }
        return "SansSerif";
    }

    // Cached fonts — created once, reused every repaint
    private static final Font FONT_RANK_CENTER = new Font("SansSerif",   Font.BOLD,  22);
    private static final Font FONT_SUIT_CENTER = new Font(SUIT_FONT_NAME, Font.PLAIN, 26);
    private static final Font FONT_RANK_MINI   = new Font("SansSerif",   Font.BOLD,  11);
    private static final Font FONT_SUIT_MINI   = new Font(SUIT_FONT_NAME, Font.PLAIN, 11);

    // Cached strokes
    private static final BasicStroke STROKE_BORDER       = new BasicStroke(1.2f);
    private static final BasicStroke STROKE_BACK_OUTER   = new BasicStroke(2f);
    private static final BasicStroke STROKE_BACK_INNER   = new BasicStroke(1f);
    private static final BasicStroke STROKE_BACK_PATTERN = new BasicStroke(0.5f);

    // Cached colors
    private static final Color SHADOW_COLOR       = new Color(0, 0, 0, 60);
    private static final Color INNER_BORDER_COLOR = new Color(255, 255, 255, 80);
    private static final Color HIGHLIGHT_BG       = new Color(255, 255, 180);

    // Card back image cache — the diamond-grid pattern is expensive; render once per theme
    private static BufferedImage cachedCardBack = null;
    private static GameTheme     cachedCardBackTheme = null;

    /** Call this when the theme changes so the card back is re-rendered. */
    public static void invalidateCardBackCache() {
        cachedCardBack = null;
        cachedCardBackTheme = null;
    }

    private static BufferedImage getCardBackImage(GameTheme theme) {
        if (cachedCardBack == null || cachedCardBackTheme != theme) {
            // +3 to accommodate the shadow offset
            cachedCardBack = new BufferedImage(CARD_W + 3, CARD_H + 3, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg = cachedCardBack.createGraphics();
            bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            renderCardBack(bg, 0, 0, theme);
            bg.dispose();
            cachedCardBackTheme = theme;
        }
        return cachedCardBack;
    }

    public static void drawCard(Graphics2D g2, Card card, int x, int y, GameTheme theme, boolean highlighted) {
        // Shadow
        g2.setColor(SHADOW_COLOR);
        g2.fill(new RoundRectangle2D.Double(x + 3, y + 3, CARD_W, CARD_H, 10, 10));

        // Card body
        g2.setColor(highlighted ? HIGHLIGHT_BG : theme.getCardBackground());
        g2.fill(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, 10, 10));

        // Border
        g2.setColor(theme.getCardBorder());
        g2.setStroke(STROKE_BORDER);
        g2.draw(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, 10, 10));

        Color textColor = card.getSuit().isRed() ? theme.getRedColor() : theme.getBlackColor();
        g2.setColor(textColor);

        String rankStr = card.getRank().getSymbol();
        String suitStr = card.getSuit().getSymbol();

        // Center rank
        g2.setFont(FONT_RANK_CENTER);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(rankStr, x + (CARD_W - fm.stringWidth(rankStr)) / 2, y + 38);

        // Center suit
        g2.setFont(FONT_SUIT_CENTER);
        fm = g2.getFontMetrics();
        g2.drawString(suitStr, x + (CARD_W - fm.stringWidth(suitStr)) / 2, y + 68);

        // Mini top-left
        g2.setFont(FONT_RANK_MINI);
        g2.drawString(rankStr, x + 5, y + 14);
        g2.setFont(FONT_SUIT_MINI);
        g2.drawString(suitStr, x + 5, y + 26);

        // Mini bottom-right (rotated 180°) — save/restore transform instead of g2.create()
        AffineTransform saved = g2.getTransform();
        g2.rotate(Math.PI, x + CARD_W / 2.0, y + CARD_H / 2.0);
        g2.setFont(FONT_RANK_MINI);
        g2.drawString(rankStr, x + 5, y + 14);
        g2.setFont(FONT_SUIT_MINI);
        g2.drawString(suitStr, x + 5, y + 26);
        g2.setTransform(saved);
    }

    public static void drawCardBack(Graphics2D g2, int x, int y, GameTheme theme) {
        g2.drawImage(getCardBackImage(theme), x, y, null);
    }

    public static void drawCardBackRotated(Graphics2D g2, int x, int y, double angle, GameTheme theme) {
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.rotate(angle, x + CARD_W / 2.0, y + CARD_H / 2.0);
        g2r.drawImage(getCardBackImage(theme), x, y, null);
        g2r.dispose();
    }

    /** Actual rendering of the card back — called once per theme to populate the cache. */
    private static void renderCardBack(Graphics2D g2, int x, int y, GameTheme theme) {
        // Shadow
        g2.setColor(SHADOW_COLOR);
        g2.fill(new RoundRectangle2D.Double(x + 3, y + 3, CARD_W, CARD_H, 10, 10));

        // Card back fill
        g2.setColor(theme.getCardBackPrimary());
        g2.fill(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, 10, 10));

        // Outer border
        g2.setColor(Color.WHITE);
        g2.setStroke(STROKE_BACK_OUTER);
        g2.draw(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, 10, 10));

        // Inner border
        g2.setColor(INNER_BORDER_COLOR);
        g2.setStroke(STROKE_BACK_INNER);
        g2.draw(new RoundRectangle2D.Double(x + 5, y + 5, CARD_W - 10, CARD_H - 10, 6, 6));

        // Diamond grid pattern
        g2.setColor(theme.getCardBackSecondary());
        g2.setStroke(STROKE_BACK_PATTERN);
        for (int i = 0; i < CARD_W; i += 8) {
            for (int j = 0; j < CARD_H; j += 8) {
                int cx = x + 8 + i;
                int cy = y + 8 + j;
                if (cx > x + 6 && cx < x + CARD_W - 6 && cy > y + 6 && cy < y + CARD_H - 6) {
                    g2.drawLine(cx - 2, cy, cx + 2, cy);
                    g2.drawLine(cx, cy - 2, cx, cy + 2);
                }
            }
        }
    }
}
