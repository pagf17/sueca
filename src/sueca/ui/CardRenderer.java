package sueca.ui;

import sueca.model.Card;
import sueca.model.Rank;
import sueca.model.Suit;
import sueca.ui.theme.GameTheme;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CardRenderer {

    public static final int CARD_W = 70;
    public static final int CARD_H = 100;

    public static void drawCard(Graphics2D g2, Card card, int x, int y, GameTheme theme, boolean highlighted) {
        // Shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new RoundRectangle2D.Double(x + 3, y + 3, CARD_W, CARD_H, 10, 10));

        // Card body
        if (highlighted) {
            g2.setColor(new Color(255, 255, 180));
        } else {
            g2.setColor(theme.getCardBackground());
        }
        g2.fill(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, 10, 10));

        // Border
        g2.setColor(theme.getCardBorder());
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, 10, 10));

        // Color based on suit
        Color textColor = card.getSuit().isRed() ? theme.getRedColor() : theme.getBlackColor();
        g2.setColor(textColor);

        String rankStr = card.getRank().getSymbol();
        String suitStr = card.getSuit().getSymbol();

        // Center rank and suit
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int rankW = fm.stringWidth(rankStr);
        g2.drawString(rankStr, x + (CARD_W - rankW) / 2, y + 38);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 26));
        fm = g2.getFontMetrics();
        int suitW = fm.stringWidth(suitStr);
        g2.drawString(suitStr, x + (CARD_W - suitW) / 2, y + 68);

        // Mini rank top-left
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString(rankStr, x + 5, y + 14);

        // Mini suit below rank
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(suitStr, x + 5, y + 26);

        // Mini rank bottom-right (rotated)
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.rotate(Math.PI, x + CARD_W / 2.0, y + CARD_H / 2.0);
        g2r.setColor(textColor);
        g2r.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2r.drawString(rankStr, x + 5, y + 14);
        g2r.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2r.drawString(suitStr, x + 5, y + 26);
        g2r.dispose();
    }

    public static void drawCardBack(Graphics2D g2, int x, int y, GameTheme theme) {
        // Shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new RoundRectangle2D.Double(x + 3, y + 3, CARD_W, CARD_H, 10, 10));

        // Card back
        g2.setColor(theme.getCardBackPrimary());
        g2.fill(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, 10, 10));

        // Border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, 10, 10));

        // Inner border
        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Double(x + 5, y + 5, CARD_W - 10, CARD_H - 10, 6, 6));

        // Pattern - simple diamond grid
        g2.setColor(theme.getCardBackSecondary());
        g2.setStroke(new BasicStroke(0.5f));
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

    public static void drawCardBackRotated(Graphics2D g2, int x, int y, double angle, GameTheme theme) {
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.rotate(angle, x + CARD_W / 2.0, y + CARD_H / 2.0);
        drawCardBack(g2r, x, y, theme);
        g2r.dispose();
    }
}
