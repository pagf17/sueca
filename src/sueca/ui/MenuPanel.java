package sueca.ui;

import sueca.model.Difficulty;
import sueca.ui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class MenuPanel extends JPanel {

    public interface MenuListener {
        void onStartGame(Difficulty difficulty, GameTheme theme);
        void onToggleFullscreen();
        boolean isFullscreen();
    }

    private MenuListener listener;
    private GameTheme[] themes;
    private Difficulty selectedDifficulty = Difficulty.MEDIA;
    private int selectedThemeIndex = 0;

    private Rectangle[] diffButtons = new Rectangle[3];
    private Rectangle[] themeButtons = new Rectangle[2];
    private Rectangle playButton = new Rectangle();
    private Rectangle exitButton = new Rectangle();
    private Rectangle fullscreenButton = new Rectangle();
    private int hoverIndex = -1; // 0-2 diff, 3-4 theme, 5 play, 6 exit, 7 fullscreen

    public MenuPanel(MenuListener listener, GameTheme[] themes) {
        this.listener = listener;
        this.themes = themes;
        setPreferredSize(new Dimension(1024, 700));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e.getX(), e.getY());
            }
        });
    }

    private void handleClick(int x, int y) {
        Difficulty[] diffs = Difficulty.values();
        for (int i = 0; i < 3; i++) {
            if (diffButtons[i] != null && diffButtons[i].contains(x, y)) {
                selectedDifficulty = diffs[i];
                repaint();
                return;
            }
        }
        for (int i = 0; i < 2; i++) {
            if (themeButtons[i] != null && themeButtons[i].contains(x, y)) {
                selectedThemeIndex = i;
                repaint();
                return;
            }
        }
        if (playButton.contains(x, y)) {
            listener.onStartGame(selectedDifficulty, themes[selectedThemeIndex]);
            return;
        }
        if (exitButton.contains(x, y)) {
            System.exit(0);
        }
        if (fullscreenButton.contains(x, y)) {
            listener.onToggleFullscreen();
            repaint();
        }
    }

    private void updateHover(int x, int y) {
        int old = hoverIndex;
        hoverIndex = -1;
        for (int i = 0; i < 3; i++) {
            if (diffButtons[i] != null && diffButtons[i].contains(x, y)) hoverIndex = i;
        }
        for (int i = 0; i < 2; i++) {
            if (themeButtons[i] != null && themeButtons[i].contains(x, y)) hoverIndex = 3 + i;
        }
        if (playButton.contains(x, y)) hoverIndex = 5;
        if (exitButton.contains(x, y)) hoverIndex = 6;
        if (fullscreenButton.contains(x, y)) hoverIndex = 7;
        if (old != hoverIndex) {
            setCursor(hoverIndex >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth(), h = getHeight();

        // Background
        Point2D center = new Point2D.Float(w / 2f, h / 2f);
        float radius = Math.max(w, h) * 0.7f;
        Color c1 = new Color(58, 122, 58);
        Color c2 = new Color(30, 70, 30);
        RadialGradientPaint bg = new RadialGradientPaint(center, radius,
                new float[]{0f, 1f}, new Color[]{c1, c2});
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // Decorative suits
        g2.setFont(new Font("SansSerif", Font.PLAIN, 150));
        g2.setColor(new Color(0, 0, 0, 15));
        g2.drawString("♣", 30, 170);
        g2.drawString("♠", w - 170, 170);
        g2.drawString("♦", 30, h - 30);
        g2.drawString("♥", w - 170, h - 30);

        int cx = w / 2;

        // Fullscreen button (top-right corner)
        drawFullscreenButton(g2, w);

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 64));
        String title = "Sueca";
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(title);

        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawString(title, cx - tw / 2 + 3, 93);

        g2.setColor(new Color(130, 195, 255));
        g2.drawString(title, cx - tw / 2, 90);

        // Octagon behind options
        Path2D oct = createOctagon(cx, h / 2 + 40, 500, 400);
        g2.setColor(new Color(107, 58, 31));
        g2.setStroke(new BasicStroke(4f));
        g2.draw(oct);
        g2.setColor(new Color(50, 100, 50, 100));
        g2.fill(oct);

        int baseY = 150;

        // Difficulty section
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        String diffLabel = "Dificuldade";
        fm = g2.getFontMetrics();
        g2.drawString(diffLabel, cx - fm.stringWidth(diffLabel) / 2, baseY);

        Difficulty[] diffs = Difficulty.values();
        int btnW = 140, btnH = 40, btnGap = 15;
        int totalBtnW = 3 * btnW + 2 * btnGap;
        int btnStartX = cx - totalBtnW / 2;

        for (int i = 0; i < 3; i++) {
            int bx = btnStartX + i * (btnW + btnGap);
            int by = baseY + 15;
            diffButtons[i] = new Rectangle(bx, by, btnW, btnH);

            boolean selected = diffs[i] == selectedDifficulty;
            boolean hover = hoverIndex == i;

            Color btnColor = selected ? new Color(255, 180, 50) :
                    hover ? new Color(90, 170, 255) : new Color(74, 158, 255);

            g2.setColor(new Color(0, 0, 0, 40));
            g2.fill(new RoundRectangle2D.Double(bx + 2, by + 2, btnW, btnH, 10, 10));

            g2.setColor(btnColor);
            g2.fill(new RoundRectangle2D.Double(bx, by, btnW, btnH, 10, 10));

            if (selected) {
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(bx, by, btnW, btnH, 10, 10));
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            fm = g2.getFontMetrics();
            String label = diffs[i].getLabel();
            g2.drawString(label, bx + (btnW - fm.stringWidth(label)) / 2, by + 26);
        }

        // Theme section
        baseY += 90;
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        String themeLabel = "Tema Visual";
        fm = g2.getFontMetrics();
        g2.drawString(themeLabel, cx - fm.stringWidth(themeLabel) / 2, baseY);

        int tbtnW = 180;
        int totalTbtnW = 2 * tbtnW + btnGap;
        int tbtnStartX = cx - totalTbtnW / 2;

        for (int i = 0; i < 2; i++) {
            int bx = tbtnStartX + i * (tbtnW + btnGap);
            int by = baseY + 15;
            themeButtons[i] = new Rectangle(bx, by, tbtnW, btnH);

            boolean selected = i == selectedThemeIndex;
            boolean hover = hoverIndex == 3 + i;

            Color btnColor = selected ? new Color(255, 180, 50) :
                    hover ? new Color(90, 170, 255) : new Color(74, 158, 255);

            g2.setColor(new Color(0, 0, 0, 40));
            g2.fill(new RoundRectangle2D.Double(bx + 2, by + 2, tbtnW, btnH, 10, 10));

            g2.setColor(btnColor);
            g2.fill(new RoundRectangle2D.Double(bx, by, tbtnW, btnH, 10, 10));

            if (selected) {
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(bx, by, tbtnW, btnH, 10, 10));
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            fm = g2.getFontMetrics();
            String name = themes[i].getName();
            g2.drawString(name, bx + (tbtnW - fm.stringWidth(name)) / 2, by + 26);
        }

        // Theme preview colors
        for (int i = 0; i < 2; i++) {
            int bx = tbtnStartX + i * (tbtnW + btnGap);
            int by = baseY + 60;
            GameTheme t = themes[i];

            int swSize = 18;
            int gap = 4;
            Color[] swatches = {t.getFeltColor(), t.getCardBackPrimary(), t.getLabelTeamUs(), t.getAccentColor()};
            int swTotalW = swatches.length * (swSize + gap) - gap;
            int swStartX = bx + (tbtnW - swTotalW) / 2;

            for (int j = 0; j < swatches.length; j++) {
                g2.setColor(swatches[j]);
                g2.fillRoundRect(swStartX + j * (swSize + gap), by, swSize, swSize, 4, 4);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(swStartX + j * (swSize + gap), by, swSize, swSize, 4, 4);
            }
        }

        // Play button
        baseY += 120;
        int pbtnW = 260, pbtnH = 62;
        int pbx = cx - pbtnW / 2;
        playButton = new Rectangle(pbx, baseY, pbtnW, pbtnH);

        boolean playHover = hoverIndex == 5;

        if (playHover) {
            for (int glow = 12; glow > 0; glow -= 3) {
                g2.setColor(new Color(60, 220, 100, 30));
                g2.setStroke(new BasicStroke(glow));
                g2.draw(new RoundRectangle2D.Double(pbx - glow / 2, baseY - glow / 2,
                        pbtnW + glow, pbtnH + glow, 18, 18));
            }
        }

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fill(new RoundRectangle2D.Double(pbx + 4, baseY + 4, pbtnW, pbtnH, 18, 18));

        GradientPaint playGrad = playHover
                ? new GradientPaint(pbx, baseY, new Color(80, 220, 120), pbx, baseY + pbtnH, new Color(30, 170, 70))
                : new GradientPaint(pbx, baseY, new Color(60, 200, 100), pbx, baseY + pbtnH, new Color(20, 140, 50));
        g2.setPaint(playGrad);
        g2.fill(new RoundRectangle2D.Double(pbx, baseY, pbtnW, pbtnH, 18, 18));

        g2.setColor(new Color(255, 255, 255, 55));
        g2.fill(new RoundRectangle2D.Double(pbx + 4, baseY + 2, pbtnW - 8, pbtnH / 2, 14, 14));

        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(pbx, baseY, pbtnW, pbtnH, 18, 18));

        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.setColor(new Color(0, 0, 0, 60));
        fm = g2.getFontMetrics();
        String play = "▶  Jogar";
        g2.drawString(play, cx - fm.stringWidth(play) / 2 + 2, baseY + 41);
        g2.setColor(Color.WHITE);
        g2.drawString(play, cx - fm.stringWidth(play) / 2, baseY + 39);

        // Exit button
        int ebtnW = 160, ebtnH = 44;
        int exitY = baseY + pbtnH + 22;
        int ebx = cx - ebtnW / 2;
        exitButton = new Rectangle(ebx, exitY, ebtnW, ebtnH);

        boolean exitHover = hoverIndex == 6;
        Color exitColor = exitHover ? new Color(210, 60, 60) : new Color(170, 40, 40);

        g2.setColor(new Color(0, 0, 0, 50));
        g2.fill(new RoundRectangle2D.Double(ebx + 3, exitY + 3, ebtnW, ebtnH, 12, 12));

        g2.setColor(exitColor);
        g2.fill(new RoundRectangle2D.Double(ebx, exitY, ebtnW, ebtnH, 12, 12));

        if (exitHover) {
            g2.setColor(new Color(255, 255, 255, 50));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(ebx, exitY, ebtnW, ebtnH, 12, 12));
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String exit = "Sair";
        g2.drawString(exit, cx - fm.stringWidth(exit) / 2, exitY + 28);
    }

    private void drawFullscreenButton(Graphics2D g2, int w) {
        boolean fs = listener.isFullscreen();
        String label = fs ? "Modo Janela" : "Ecra Inteiro";
        boolean hover = hoverIndex == 7;

        int btnW = 148, btnH = 36;
        int bx = w - btnW - 14;
        int by = 14;
        fullscreenButton = new Rectangle(bx, by, btnW, btnH);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fill(new RoundRectangle2D.Double(bx + 2, by + 2, btnW, btnH, 10, 10));

        // Background
        Color bg = hover ? new Color(90, 170, 255) : new Color(40, 100, 40, 200);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(bx, by, btnW, btnH, 10, 10));

        // Border
        g2.setColor(new Color(255, 255, 255, hover ? 80 : 50));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(bx, by, btnW, btnH, 10, 10));

        // Icon + label
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String icon = fs ? "[ ]" : "[+]";
        String full = icon + "  " + label;
        g2.drawString(full, bx + (btnW - fm.stringWidth(full)) / 2, by + 23);
    }

    private Path2D createOctagon(int cx, int cy, int w, int h) {
        Path2D path = new Path2D.Double();
        double hw = w / 2.0, hh = h / 2.0;
        double cut = 0.25;
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
}
