package sueca.ui.theme;

import java.awt.*;

public class ClassicTheme implements GameTheme {
    @Override public String getName() { return "Clássico"; }

    @Override public Color getFeltColor() { return new Color(58, 122, 58); }
    @Override public Color getFeltDarker() { return new Color(45, 94, 45); }
    @Override public Color getTableBorderColor() { return new Color(107, 58, 31); }

    @Override public Color getCardBackground() { return new Color(255, 254, 245); }
    @Override public Color getCardBorder() { return new Color(180, 170, 155); }
    @Override public Color getRedColor() { return new Color(196, 30, 30); }
    @Override public Color getBlackColor() { return new Color(26, 26, 46); }
    @Override public Color getCardBackPrimary() { return new Color(34, 85, 170); }
    @Override public Color getCardBackSecondary() { return new Color(26, 61, 122); }

    @Override public Color getButtonColor() { return new Color(74, 158, 255); }
    @Override public Color getButtonHoverColor() { return new Color(53, 128, 219); }
    @Override public Color getButtonTextColor() { return Color.WHITE; }
    @Override public Color getPanelBackground() { return new Color(240, 245, 250, 235); }
    @Override public Color getPanelBorder() { return new Color(255, 255, 255, 128); }
    @Override public Color getTextColor() { return new Color(51, 51, 51); }
    @Override public Color getAccentColor() { return new Color(255, 215, 0); }
    @Override public Color getLabelTeamUs() { return new Color(74, 158, 255); }
    @Override public Color getLabelTeamThem() { return new Color(224, 112, 64); }

    @Override public Font getTitleFont() { return new Font("SansSerif", Font.BOLD, 52); }
    @Override public Font getButtonFont() { return new Font("SansSerif", Font.BOLD, 18); }
    @Override public Font getCardFont() { return new Font("SansSerif", Font.BOLD, 16); }
    @Override public Font getLabelFont() { return new Font("SansSerif", Font.BOLD, 13); }
    @Override public Font getScoreFont() { return new Font("SansSerif", Font.BOLD, 14); }
}
