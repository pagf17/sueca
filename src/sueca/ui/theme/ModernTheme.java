package sueca.ui.theme;

import java.awt.*;

public class ModernTheme implements GameTheme {
    @Override public String getName() { return "Moderno"; }

    @Override public Color getFeltColor() { return new Color(30, 40, 55); }
    @Override public Color getFeltDarker() { return new Color(20, 28, 40); }
    @Override public Color getTableBorderColor() { return new Color(60, 80, 110); }

    @Override public Color getCardBackground() { return new Color(245, 245, 250); }
    @Override public Color getCardBorder() { return new Color(160, 165, 180); }
    @Override public Color getRedColor() { return new Color(220, 50, 50); }
    @Override public Color getBlackColor() { return new Color(30, 30, 50); }
    @Override public Color getCardBackPrimary() { return new Color(80, 60, 140); }
    @Override public Color getCardBackSecondary() { return new Color(50, 35, 100); }

    @Override public Color getButtonColor() { return new Color(100, 80, 180); }
    @Override public Color getButtonHoverColor() { return new Color(120, 100, 200); }
    @Override public Color getButtonTextColor() { return Color.WHITE; }
    @Override public Color getPanelBackground() { return new Color(35, 45, 65, 235); }
    @Override public Color getPanelBorder() { return new Color(70, 90, 120, 128); }
    @Override public Color getTextColor() { return new Color(220, 225, 235); }
    @Override public Color getAccentColor() { return new Color(255, 180, 50); }
    @Override public Color getLabelTeamUs() { return new Color(80, 140, 220); }
    @Override public Color getLabelTeamThem() { return new Color(220, 100, 60); }

    @Override public Font getTitleFont() { return new Font("SansSerif", Font.BOLD, 52); }
    @Override public Font getButtonFont() { return new Font("SansSerif", Font.BOLD, 18); }
    @Override public Font getCardFont() { return new Font("SansSerif", Font.BOLD, 16); }
    @Override public Font getLabelFont() { return new Font("SansSerif", Font.BOLD, 13); }
    @Override public Font getScoreFont() { return new Font("SansSerif", Font.BOLD, 14); }
}
