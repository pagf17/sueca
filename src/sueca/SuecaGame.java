package sueca;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import sueca.model.*;
import sueca.ui.*;
import sueca.ui.theme.*;

public class SuecaGame extends JFrame implements MenuPanel.MenuListener, GamePanel.PauseListener {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private GameEngine engine;
    private boolean fullscreen = false;

    private final GameTheme[] themes = {new ClassicTheme(), new ModernTheme()};
    private int currentThemeIndex = 0;

    public SuecaGame() {
        setTitle("Sueca");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(900, 650));

        try {
            java.io.InputStream is = getClass().getResourceAsStream("/sueca/resources/logo.png");
            if (is != null) {
                BufferedImage original = ImageIO.read(is);
                if (original != null) {
                    List<Image> icons = new ArrayList<>();
                    for (int size : new int[]{16, 32, 48, 64, 128, 256}) {
                        icons.add(original.getScaledInstance(size, size, Image.SCALE_SMOOTH));
                    }
                    setIconImages(icons);
                }
            }
        } catch (Exception ignored) {}

        engine = new GameEngine();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        menuPanel = new MenuPanel(this, themes);
        mainPanel.add(menuPanel, "menu");

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);

        cardLayout.show(mainPanel, "menu");
    }

    @Override
    public void onStartGame(Difficulty difficulty, GameTheme theme) {
        // Sync theme index with whatever was selected in the menu
        for (int i = 0; i < themes.length; i++) {
            if (themes[i].getClass() == theme.getClass()) {
                currentThemeIndex = i;
                break;
            }
        }
        engine = new GameEngine();
        gamePanel = new GamePanel(engine, themes[currentThemeIndex], difficulty, this);
        mainPanel.add(gamePanel, "game");
        cardLayout.show(mainPanel, "game");
        gamePanel.requestFocusInWindow();

        SwingUtilities.invokeLater(() -> gamePanel.startRound());
    }

    // ─── MenuListener / PauseListener shared ───

    @Override
    public void onToggleFullscreen() {
        if (!fullscreen) {
            dispose();
            setUndecorated(true);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = ge.getMaximumWindowBounds();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(this);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setBounds(bounds);
            }
            setVisible(true);
            fullscreen = true;
        } else {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            if (gd.getFullScreenWindow() == this) {
                gd.setFullScreenWindow(null);
            }
            dispose();
            setUndecorated(false);
            setExtendedState(JFrame.NORMAL);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            fullscreen = false;
        }
        SwingUtilities.invokeLater(() -> {
            if (gamePanel != null && gamePanel.isShowing()) {
                gamePanel.requestFocusInWindow();
            } else {
                menuPanel.requestFocusInWindow();
            }
            mainPanel.repaint();
        });
    }

    @Override
    public boolean isFullscreen() {
        return fullscreen;
    }

    // ─── PauseListener only ───

    @Override
    public void onGoToMenu() {
        cardLayout.show(mainPanel, "menu");
        menuPanel.requestFocusInWindow();
    }

    @Override
    public void onExitGame() {
        System.exit(0);
    }

    @Override
    public void onCycleTheme() {
        currentThemeIndex = (currentThemeIndex + 1) % themes.length;
        if (gamePanel != null) {
            gamePanel.setTheme(themes[currentThemeIndex]);
        }
    }

    @Override
    public String getCurrentThemeName() {
        return themes[currentThemeIndex].getName();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SuecaGame game = new SuecaGame();
            game.setVisible(true);
        });
    }
}
